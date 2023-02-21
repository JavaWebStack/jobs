package org.javawebstack.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.javawebstack.jobs.handler.JobExceptionHandler;
import org.javawebstack.jobs.handler.retry.JobRetryHandler;
import org.javawebstack.jobs.handler.retry.RetryContext;
import org.javawebstack.jobs.handler.retry.RetryReason;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.serialization.JobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.*;
import org.javawebstack.jobs.util.HostnameUtil;
import org.javawebstack.jobs.util.JobExitException;
import org.javawebstack.jobs.util.SyncTimer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JobWorker {

    @Getter
    final Jobs jobs;
    @Getter
    final String queue;
    final int pollInterval;
    final int workerThreads;
    boolean stopRequested = false;
    @Getter
    boolean running = false;
    ThreadPoolExecutor executor;
    final JobWorkerInfo workerInfo;

    public JobWorker(Jobs jobs, String queue, int workerThreads, int pollInterval) {
        this.jobs = jobs;
        this.queue = queue;
        this.pollInterval = pollInterval;
        this.workerThreads = workerThreads;
        workerInfo = new JobWorkerInfo()
                .setOnline(true)
                .setQueue(queue)
                .setThreads(workerThreads)
                .setHostname(HostnameUtil.getHostname());
        if(workerInfo.getHostname() == null)
            workerInfo.setHostname("unknown");
        jobs.getStorage().createWorker(workerInfo);
        start();
    }

    public void stop() {
        stopRequested = true;
        executor.shutdown();
    }

    public void start() {
        if(running)
            throw new IllegalStateException("Worker is already running");
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(workerThreads);
        new Thread(new JobScheduling()).start();
        running = true;
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private class JobScheduling implements Runnable {

        public void run() {
            JobStorage storage = jobs.getStorage();
            JobScheduler scheduler = jobs.getScheduler();
            running = true;
            stopRequested = false;
            SyncTimer heartbeat = new SyncTimer(() -> {
                storage.setWorkerOnline(workerInfo.getId(), true);
            }, 0, 15000);
            SyncTimer poll = new SyncTimer(() -> {
                try {
                    UUID id = scheduler.poll(queue);
                    if(id != null) {
                        JobInfo info = storage.getJob(id);
                        if(info == null)
                            return;
                        executor.execute(new JobExecution(info));
                    } else {
                        Thread.sleep(pollInterval);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Failed to get job
                }
            }, 0, pollInterval);
            SyncTimer processSchedule = new SyncTimer(() -> {
                scheduler.processSchedule(queue).forEach(id -> {
                    JobEvent.createEnqueued(storage, id, queue);
                    storage.setJobStatus(id, JobStatus.ENQUEUED);
                });
            }, 0, pollInterval);
            SyncTimer processRecurring = new SyncTimer(() -> {
                storage.queryRecurringJobs(new RecurringJobQuery()).forEach(recurringJob -> {
                    Date nextExecution = recurringJob.getCron().next(recurringJob.getLastExecutionAt());
                    if (recurringJob.getLastJobId() == null) {
                        UUID newId = jobs.schedule(recurringJob.getQueue(), nextExecution, recurringJob.getType(), recurringJob.getPayload());
                        storage.updateRecurringJob(recurringJob.getId(), newId, nextExecution);
                    } else {
                        JobInfo info = storage.getJob(recurringJob.getLastJobId());
                        if (info == null || info.getStatus() == JobStatus.SUCCESS || info.getStatus() == JobStatus.FAILED) { // processing is done at this point
                            UUID newId = jobs.schedule(recurringJob.getQueue(), nextExecution, recurringJob.getType(), recurringJob.getPayload());
                            storage.updateRecurringJob(recurringJob.getId(), newId, nextExecution);
                        }
                    }
                });
            }, 0, pollInterval);
            while (!stopRequested) {
                boolean ticked = heartbeat.tick();
                ticked = ticked || processSchedule.tick();
                ticked = ticked || poll.tick();
                ticked = ticked || processRecurring.tick();
                if(!ticked) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {}
                }
            }
            storage.setWorkerOnline(workerInfo.getId(), false);
            running = false;
        }

    }

    @AllArgsConstructor
    private class JobExecution implements Runnable {

        JobInfo info;

        public void run() {
            JobSerializer serializer = jobs.getSerializer();
            JobStorage storage = jobs.getStorage();
            UUID id = info.getId();

            storage.setJobStatus(id, JobStatus.PROCESSING);
            JobEvent event = new JobEvent()
                    .setJobId(id)
                    .setType(JobEvent.Type.PROCESSING);
            storage.createEvent(event);
            storage.createLogEntry(new JobLogEntry()
                    .setEventId(event.getId())
                    .setLevel(LogLevel.INFO)
                    .setMessage("Processing on worker '" + workerInfo.getId() + "'")
            );
            String payload = storage.getJobPayload(id);
            Job job;
            try {
                job = serializer.deserialize((Class<? extends Job>) Class.forName(info.getType()), payload);
            } catch (Exception ex) {
                event = new JobEvent()
                        .setJobId(id)
                        .setType(JobEvent.Type.FAILED);
                storage.createEvent(event);
                storage.createLogEntry(new JobLogEntry()
                        .setEventId(event.getId())
                        .setLevel(LogLevel.ERROR)
                        .setMessage("Failed to deserialize the job: " + ex.getMessage())
                );
                storage.setJobStatus(id, JobStatus.FAILED);
                return;
            }
            JobContext context = new JobContext(jobs, info, queue, event);
            try {
                job.execute(context);
                event = new JobEvent()
                        .setJobId(id)
                        .setType(JobEvent.Type.SUCCESS);
                storage.createEvent(event);
                storage.setJobStatus(id, JobStatus.SUCCESS);
            } catch (Throwable t) {
                JobRetryHandler retryHandler = (job instanceof JobRetryHandler) ? ((JobRetryHandler) job) : jobs.getDefaultRetryHandler();
                if(t instanceof JobExitException) {
                    JobExitException jee = (JobExitException) t;
                    if(!jee.isSuccess()) {
                        event = new JobEvent()
                                .setJobId(id)
                                .setType(JobEvent.Type.FAILED);
                        storage.createEvent(event);
                        if(jee.getMessage() != null) {
                            storage.createLogEntry(new JobLogEntry()
                                    .setEventId(event.getId())
                                    .setLevel(LogLevel.ERROR)
                                    .setMessage(jee.getMessage())
                            );
                        }
                    }
                    if(jee.isRetry()) {
                        if(jee.getRetryInSeconds() != null) {
                            if(jee.getRetryInSeconds() <= 0)
                                jobs.enqueue(queue, info.getType(), payload);
                            else
                                jobs.schedule(queue, Date.from(Instant.now().plusSeconds(jee.getRetryInSeconds())), info.getType(), payload);
                        } else {
                            retryHandler.handleRetry(context, new RetryContext(jee.isSuccess() ? RetryReason.REQUESTED : RetryReason.FAILURE, null));
                        }
                    } else {
                        if(jee.isSuccess()) {
                            event = new JobEvent()
                                    .setJobId(id)
                                    .setType(JobEvent.Type.SUCCESS);
                            storage.createEvent(event);
                            if(jee.getMessage() != null) {
                                storage.createLogEntry(new JobLogEntry()
                                        .setEventId(event.getId())
                                        .setLevel(LogLevel.INFO)
                                        .setMessage(jee.getMessage())
                                );
                            }
                        }
                    }
                    if(jee.getRetryInSeconds() == -1 || !jee.isSuccess()) {
                        event = new JobEvent()
                                .setJobId(id)
                                .setType(jee.isSuccess() ? JobEvent.Type.SUCCESS : JobEvent.Type.FAILED);
                        storage.createEvent(event);
                        if(jee.getMessage() != null) {
                            storage.createLogEntry(new JobLogEntry()
                                    .setEventId(event.getId())
                                    .setLevel(jee.isSuccess() ? LogLevel.INFO : LogLevel.ERROR)
                                    .setMessage(jee.getMessage())
                            );
                        }
                    }
                    if(jee.getRetryInSeconds() != -1) {
                        if(jee.getRetryInSeconds() > 0) {
                            Date nextTry = Date.from(Instant.now().plusSeconds(jee.getRetryInSeconds()));
                            jobs.getScheduler().schedule(queue, nextTry, info.getId());
                            JobEvent.createScheduled(storage, info.getId(), nextTry, queue);
                            storage.setJobStatus(id, JobStatus.SCHEDULED);
                        } else {
                            jobs.getScheduler().enqueue(queue, info.getId());
                            JobEvent.createEnqueued(storage, info.getId(), queue);
                            storage.setJobStatus(id, JobStatus.ENQUEUED);
                        }
                    } else {
                        storage.setJobStatus(id, jee.isSuccess() ? JobStatus.SUCCESS : JobStatus.FAILED);
                    }
                    return;
                }
                if(job instanceof JobExceptionHandler)
                    ((JobExceptionHandler) job).handleException(context, t);
                jobs.getExceptionHandlers().forEach(h -> h.handleException(context, t));
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                event = new JobEvent()
                        .setJobId(id)
                        .setType(JobEvent.Type.FAILED);
                storage.createEvent(event);
                storage.createLogEntry(new JobLogEntry()
                        .setEventId(event.getId())
                        .setLevel(LogLevel.ERROR)
                        .setMessage("An exception occured during the execution:\n" + sw)
                );
                storage.setJobStatus(id, JobStatus.FAILED);
                RetryContext retryContext = new RetryContext(RetryReason.EXCEPTION, t);
                retryHandler.handleRetry(context, retryContext);
            }
        }

    }

}
