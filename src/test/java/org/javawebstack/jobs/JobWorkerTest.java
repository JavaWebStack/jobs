package org.javawebstack.jobs;

import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;
import org.javawebstack.jobs.scheduler.interval.CronInterval;
import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.RecurringJobInfo;
import org.javawebstack.jobs.storage.model.RecurringJobQuery;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JobWorkerTest {

    Jobs jobs;

    @BeforeEach
    public void up() {
        jobs = new Jobs(
                new InMemoryJobStorage(),
                new InMemoryJobScheduler(),
                new JsonJobSerializer()
        );
    }

    @Test
    public void testLifecycle() throws InterruptedException {
        String queue = "test_" + UUID.randomUUID();
        JobWorker worker = new JobWorker(jobs, queue, 1, 10);
        assertTrue(worker.isRunning());
        assertEquals(queue, worker.getQueue());
        UUID recurrentJob = jobs.scheduleRecurrently(queue, "* * * * *", new NoOpJob());
        assertEquals(1, jobs.getStorage().queryRecurringJobs(new RecurringJobQuery()).size());
        UUID firstJob = jobs.enqueue(queue, new NoOpJob());
        Thread.sleep(20); // Wait for 1 intervals + 10
        assertEquals(1, NoOpJob.getExecutions(firstJob));
        Thread.sleep(5000);
        RecurringJobInfo recurringJobInfo = jobs.getStorage().getRecurringJob(recurrentJob);
        assertNotNull(recurringJobInfo);
        assertNotNull(recurringJobInfo.getLastJobId());
        UUID oldJobId = recurringJobInfo.getLastJobId();
        worker.stop();
        Thread.sleep(60); // Wait for full idle cycle + 10
        assertFalse(worker.isRunning());
        UUID secondJob = jobs.enqueue(queue, new NoOpJob());
        Thread.sleep(20); // Wait for 1 interval + 10
        assertEquals(0, NoOpJob.getExecutions(secondJob));
        worker.start();
        Thread.sleep(20); // Wait for 1 interval + 10
        assertEquals(1, NoOpJob.getExecutions(secondJob));
        Thread.sleep(60000);
        recurringJobInfo = jobs.getStorage().getRecurringJob(recurrentJob);
        assertNotEquals(oldJobId, recurringJobInfo.getLastJobId());
        worker.stop();
    }

    @Test
    public void testNoDoubleExecution() throws InterruptedException {
        String queue = "test_ " + UUID.randomUUID();
        JobWorker worker = new JobWorker(jobs, queue, 1, 10);
        UUID jobId = jobs.enqueue(queue, new NoOpJob());
        assertNotNull(jobId);
        Thread.sleep(70); // Wait for enough time to ensure that the job could've executed twice
        assertEquals(1, NoOpJob.getExecutions(jobId));
        worker.stop();
    }

    @Test
    public void testAlreadyRunningJobWorker() {
        String queue = "test_ " + UUID.randomUUID();
        JobWorker worker = new JobWorker(jobs, queue, 1, 10);
        assertTrue(worker.isRunning());
        assertThrows(IllegalStateException.class, worker::start);
    }

}
