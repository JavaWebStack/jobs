package org.javawebstack.jobs.scheduler;

import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JobSchedulerTest {

    private static final String TEST_QUEUE = "job_scheduler_test";

    protected JobScheduler scheduler;

    @BeforeAll
    public void up() {
        scheduler = initScheduler();
    }

    protected abstract JobScheduler initScheduler();

    @Test
    public void testEnqueueJob() {
        UUID jobId = UUID.randomUUID();
        scheduler.enqueue(TEST_QUEUE, jobId);
        assertTrue(scheduler.getQueueEntries(TEST_QUEUE).contains(jobId));
    }

    @Test
    public void testScheduleJob() {
        UUID jobId = UUID.randomUUID();
        Date expectedDate = Date.from(Instant.now());
        scheduler.schedule(TEST_QUEUE, expectedDate, jobId);
        JobScheduleEntry entry = scheduler.getScheduleEntries(TEST_QUEUE).stream().filter(e -> e.getJobId().equals(jobId)).findFirst().orElse(null);
        assertNotNull(entry);
        assertEquals(expectedDate.getTime() / 1000, entry.getAt().getTime() / 1000);
    }

    @Test
    public void testProcessSchedule() {
        String queue = UUID.randomUUID().toString();
        UUID jobId = UUID.randomUUID();
        scheduler.schedule(queue, Date.from(Instant.now().minusSeconds(3600)), jobId);
        assertEquals(1, scheduler.getScheduleEntries(queue).size());
        assertEquals(0, scheduler.getQueueEntries(queue).size());
        List<UUID> enqueued = scheduler.processSchedule(queue);
        assertTrue(enqueued.contains(jobId));
        assertEquals(0, scheduler.getScheduleEntries(queue).size());
        assertEquals(1, scheduler.getQueueEntries(queue).size());
    }

    @Test
    public void testPolling() {
        String queue = UUID.randomUUID().toString();
        UUID jobId = UUID.randomUUID();
        assertNull(scheduler.poll(queue));
        scheduler.enqueue(queue, jobId);
        assertEquals(jobId, scheduler.poll(queue));
        assertNull(scheduler.poll(queue));
    }

}
