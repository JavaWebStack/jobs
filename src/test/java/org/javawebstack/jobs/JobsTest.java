package org.javawebstack.jobs;

import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.storage.model.JobInfo;
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
    public void testEnqueueJob() {
        UUID id = jobs.enqueue("test", new NoOpJob());
        assertEnqueued(id);
    }

    @Test
    public void testEnqueueJobTypeAndPayload() {
        UUID id = jobs.enqueue("test", NoOpJob.class.getName(), "{}");
        assertEnqueued(id);
    }

    private void assertEnqueued(UUID id) {
        assertNotNull(id);
        JobInfo info = storage.getJob(id);
        assertNotNull(info);
        assertTrue(scheduler.getQueueEntries("test").contains(id));
    }

    @Test
    public void testScheduleJob() {
        Date at = Date.from(Instant.now().plusSeconds(3600));
        UUID id = jobs.schedule("test", at, new NoOpJob());
        assertScheduled(id, at);
    }

    @Test
    public void testScheduleJobTypeAndPayload() {
        Date at = Date.from(Instant.now().plusSeconds(3600));
        UUID id = jobs.schedule("test", at, NoOpJob.class.getName(), "{}");
        assertScheduled(id, at);
    }

    private void assertScheduled(UUID id, Date at) {
        assertNotNull(id);
        JobInfo info = storage.getJob(id);
        assertNotNull(info);
        JobScheduleEntry entry = scheduler.getScheduleEntries("test").stream().filter(e -> e.getJobId().equals(id)).findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals(at.getTime() / 1000, entry.getAt().getTime() / 1000);
    }

}
