package org.javawebstack.jobs;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.handler.JobExceptionHandler;
import org.javawebstack.jobs.handler.retry.DefaultJobRetryHandler;
import org.javawebstack.jobs.handler.retry.JobRetryHandler;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.interval.CronInterval;
import org.javawebstack.jobs.scheduler.interval.Interval;
import org.javawebstack.jobs.serialization.JobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.JobEvent;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.RecurringJobInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
public class Jobs {

    final JobStorage storage;
    final JobScheduler scheduler;
    final JobSerializer serializer;
    @Setter
    JobRetryHandler defaultRetryHandler = new DefaultJobRetryHandler();
    @Setter
    int defaultMaxRetries = 10;
    final List<JobExceptionHandler> exceptionHandlers = new ArrayList<>();

    public Jobs(JobStorage storage, JobScheduler scheduler, JobSerializer serializer) {
        this.storage = storage;
        this.scheduler = scheduler;
        this.serializer = serializer;
    }

    public Jobs addExceptionHandler(JobExceptionHandler handler) {
        this.exceptionHandlers.add(handler);
        return this;
    }

    public UUID enqueue(String queue, Job job) {
        return enqueue(queue, job, defaultMaxRetries);
    }

    public UUID enqueue(String queue, Job job, int maxRetries) {
        return enqueue(queue, job.getClass().getName(), serializer.serialize(job), maxRetries);
    }

    public UUID enqueue(String queue, String type, String payload) {
        return enqueue(queue, type, payload, defaultMaxRetries);
    }

    public UUID enqueue(String queue, String type, String payload, int maxRetries) {
        JobInfo info = new JobInfo()
                .setType(type)
                .setMaxRetries(maxRetries);
        this.storage.createJob(info, payload);
        enqueue(queue, info.getId());
        return info.getId();
    }

    public void enqueue(String queue, UUID id) {
        scheduler.enqueue(queue, id);
        JobEvent.createEnqueued(storage, id, queue);
        storage.setJobStatus(id, JobStatus.ENQUEUED);
    }

    public UUID schedule(String queue, Date at, Job job) {
        return schedule(queue, at, job, defaultMaxRetries);
    }

    public UUID schedule(String queue, Date at, Job job, int maxRetries) {
        return schedule(queue, at, job.getClass().getName(), serializer.serialize(job), maxRetries);
    }

    public UUID schedule(String queue, Date at, String type, String payload) {
        return schedule(queue, at, type, payload, defaultMaxRetries);
    }

    public UUID schedule(String queue, Date at, String type, String payload, int maxRetries) {
        JobInfo info = new JobInfo()
                .setType(type)
                .setMaxRetries(maxRetries);
        storage.createJob(info, payload);
        schedule(queue, at, info.getId());
        return info.getId();
    }

    public void schedule(String queue, Date at, UUID id) {
        scheduler.schedule(queue, at, id);
        JobEvent.createScheduled(storage, id, at, queue);
        storage.setJobStatus(id, JobStatus.SCHEDULED);
    }

    public UUID scheduleRecurrently(String queue, String cron, Job job) {
        return scheduleRecurrently(queue, cron, job.getClass().getName(), "{}");
    }

    public UUID scheduleRecurrently(String queue, String cron, String type, String payload) {
        return scheduleRecurrently(queue, new CronInterval(cron), type, payload);
    }

    public UUID scheduleRecurrently(String queue, Interval interval, String type, String payload) {
        RecurringJobInfo info = new RecurringJobInfo()
                .setQueue(queue)
                .setCron(interval)
                .setType(type)
                .setPayload(payload);
        storage.createRecurringJob(info);
        return info.getId();
    }

    public void dequeue(UUID id) {
        scheduler.dequeue(id);
        storage.setJobStatus(id, JobStatus.DELETED);
    }
}
