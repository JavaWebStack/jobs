package org.javawebstack.jobs;

import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;
import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JobWorkerTest {

    Jobs jobs;

    @BeforeAll
    public void up() {
        jobs = new Jobs(
                new InMemoryJobStorage(),
                new InMemoryJobScheduler(),
                new JsonJobSerializer()
        );
    }

    @Test
    public void testLifecycle() throws InterruptedException {
        String queue = "test_ " + UUID.randomUUID();
        JobWorker worker = new JobWorker(jobs, queue, 1, 10);
        assertTrue(worker.isRunning());
        assertEquals(queue, worker.getQueue());
        UUID firstJob = jobs.enqueue(queue, new NoOpJob());
        Thread.sleep(20); // Wait for 1 intervals + 10
        assertEquals(1, NoOpJob.getExecutions(firstJob));
        worker.stop();
        Thread.sleep(60); // Wait for full idle cycle + 10
        assertFalse(worker.isRunning());
        UUID secondJob = jobs.enqueue(queue, new NoOpJob());
        Thread.sleep(20); // Wait for 1 interval + 10
        assertEquals(0, NoOpJob.getExecutions(secondJob));
        worker.start();
        Thread.sleep(20); // Wait for 1 interval + 10
        assertEquals(1, NoOpJob.getExecutions(secondJob));
        worker.stop();
    }

    @Test
    public void testNoDoubleExecution() throws InterruptedException {
        String queue = "test_ " + UUID.randomUUID();
        JobWorker worker = new JobWorker(jobs, queue, 1, 10);
        UUID jobId = jobs.enqueue(queue, new NoOpJob());
        Thread.sleep(70); // Wait for enough time to ensure that the job could've executed twice
        assertEquals(1, NoOpJob.getExecutions(jobId));
        worker.stop();
    }

}
