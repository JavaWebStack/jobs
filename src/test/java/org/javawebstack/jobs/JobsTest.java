package org.javawebstack.jobs;

import org.javawebstack.jobs.handler.JobExceptionHandler;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.RecurringJobInfo;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JobsTest {
    private static final String TEST_QUEUE = "test";

    JobStorage storage;
    JobScheduler scheduler;
    Jobs jobs;

    @BeforeAll
    public void up() {
        storage = new InMemoryJobStorage();
        scheduler = new InMemoryJobScheduler();
        jobs = new Jobs(
                storage,
                scheduler,
                new JsonJobSerializer()
        );
    }

    @Test
    public void testAddExceptionHandler() {
        assertEquals(0, jobs.getExceptionHandlers().size());
        jobs.addExceptionHandler((context, t) -> {
            t.printStackTrace();
        });
        assertEquals(1, jobs.getExceptionHandlers().size());
    }

    @Test
    public void testEnqueueJob() {
        UUID id = jobs.enqueue(TEST_QUEUE, new NoOpJob());
        assertEnqueued(id);
    }

    @Test
    public void testEnqueueJobTypeAndPayload() {
        UUID id = jobs.enqueue(TEST_QUEUE, NoOpJob.class.getName(), "{}");
        assertEnqueued(id);
    }

    private void assertEnqueued(UUID id) {
        assertNotNull(id);
        JobInfo info = storage.getJob(id);
        assertNotNull(info);
        assertTrue(scheduler.getQueueEntries(TEST_QUEUE).contains(id));
    }

    @Test
    public void testScheduleJob() {
        Date at = Date.from(Instant.now().plusSeconds(3600));
        UUID id = jobs.schedule(TEST_QUEUE, at, new NoOpJob());
        assertScheduled(id, at);
    }

    @Test
    public void testScheduleJobTypeAndPayload() {
        Date at = Date.from(Instant.now().plusSeconds(3600));
        UUID id = jobs.schedule(TEST_QUEUE, at, NoOpJob.class.getName(), "{}");
        assertScheduled(id, at);
    }

    @Test
    public void testScheduleRecurrently() {
        UUID id = jobs.scheduleRecurrently(TEST_QUEUE, "@daily", NoOpJob.class.getName(), "{}");
        assertRecurrentlyScheduled(id);
    }

    private void assertScheduled(UUID id, Date at) {
        assertNotNull(id);
        JobInfo info = storage.getJob(id);
        assertNotNull(info);
        JobScheduleEntry entry = scheduler.getScheduleEntries("test").stream().filter(e -> e.getJobId().equals(id)).findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals(at.getTime() / 1000, entry.getAt().getTime() / 1000);
    }

    private void assertRecurrentlyScheduled(UUID id) {
        assertNotNull(id);
        RecurringJobInfo info = storage.getRecurringJob(id);
        assertNotNull(info);
    }

}
