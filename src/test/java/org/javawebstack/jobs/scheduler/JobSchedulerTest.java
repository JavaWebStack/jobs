package org.javawebstack.jobs.scheduler;

import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.*;

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
    public void testQueueOrder() {
        // UUID's are selected to be in reversed alphabetical order to ensure the storage doesn't order by id
        UUID firstJob = UUID.fromString("20000000-0000-0000-0000-000000000000");
        UUID secondJob = UUID.fromString("10000000-0000-0000-0000-000000000000");
        scheduler.enqueue(TEST_QUEUE, firstJob);
        scheduler.enqueue(TEST_QUEUE, secondJob);
        List<UUID> queue = scheduler.getQueueEntries(TEST_QUEUE);
        assertTrue(queue.size() >= 2);
        assertEquals(firstJob, queue.get(queue.size() - 2));
        assertEquals(secondJob, queue.get(queue.size() - 1));
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

    @Test
    public void testGetScheduleEntries() {
        assertEquals(0, scheduler.getScheduleEntries(new ArrayList<>()).size());
        UUID testJobId = UUID.randomUUID();
        scheduler.schedule(TEST_QUEUE, new Date(), testJobId);
        assertEquals(1, scheduler.getScheduleEntries(Collections.singletonList(testJobId)).size());
    }

}
