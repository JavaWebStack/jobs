package org.javawebstack.jobs;

import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.storage.model.JobEvent;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobLogEntry;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.javawebstack.jobs.util.JobExitException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JobContextTest {

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

    private JobContext prepareContext() {
        String queue = UUID.randomUUID().toString();
        UUID id = jobs.enqueue(queue, NoOpJob.class.getName(), "{}");
        scheduler.poll(queue);
        JobInfo info = storage.getJob(id);
        storage.setJobStatus(id, JobStatus.PROCESSING);
        JobEvent event = new JobEvent()
                .setJobId(id)
                .setType(JobEvent.Type.PROCESSING);
        storage.createEvent(event);
        return new JobContext(jobs, info, queue, event);
    }

    @Test
    public void testEnqueue() {
        JobContext context = prepareContext();
        String queue = UUID.randomUUID().toString();
        context.enqueue(queue);
        assertEquals(1, scheduler.getQueueEntries(queue).size());
    }

    @Test
    public void testSchedule() {
        JobContext context = prepareContext();
        String queue = UUID.randomUUID().toString();
        Date at = Date.from(Instant.now().plusSeconds(60));
        context.schedule(queue, at);
        List<JobScheduleEntry> entries = scheduler.getScheduleEntries(queue);
        assertEquals(1, entries.size());
        assertEquals(at.getTime() / 1000, entries.get(0).getAt().getTime() / 1000);
    }

    @Test
    public void testInfo() {
        JobContext context = prepareContext();
        context.info("a message");
        JobEvent event = storage.queryEvents(context.getId()).stream().filter(e -> e.getType() == JobEvent.Type.PROCESSING).findFirst().orElse(null);
        List<JobLogEntry> logEntries = storage.queryLogEntries(event.getId());
        JobLogEntry entry = logEntries.get(logEntries.size() - 1);
        assertEquals("a message", entry.getMessage());
        assertEquals(LogLevel.INFO, entry.getLevel());
    }

    @Test
    public void testWarning() {
        JobContext context = prepareContext();
        context.warning("a message");
        JobEvent event = storage.queryEvents(context.getId()).stream().filter(e -> e.getType() == JobEvent.Type.PROCESSING).findFirst().orElse(null);
        List<JobLogEntry> logEntries = storage.queryLogEntries(event.getId());
        JobLogEntry entry = logEntries.get(logEntries.size() - 1);
        assertEquals("a message", entry.getMessage());
        assertEquals(LogLevel.WARNING, entry.getLevel());
    }

    @Test
    public void testError() {
        JobContext context = prepareContext();
        context.error("a message");
        JobEvent event = storage.queryEvents(context.getId()).stream().filter(e -> e.getType() == JobEvent.Type.PROCESSING).findFirst().orElse(null);
        List<JobLogEntry> logEntries = storage.queryLogEntries(event.getId());
        JobLogEntry entry = logEntries.get(logEntries.size() - 1);
        assertEquals("a message", entry.getMessage());
        assertEquals(LogLevel.ERROR, entry.getLevel());
    }

    @Test
    public void testFail() {
        JobContext context = prepareContext();
        JobExitException ex = assertThrows(JobExitException.class, () -> context.fail("a message"));
        assertEquals("a message", ex.getMessage());
        assertFalse(ex.isSuccess());
        assertTrue(ex.isRetry());
        assertNull(ex.getRetryInSeconds());
    }

    @Test
    public void testFailFinally() {
        JobContext context = prepareContext();
        JobExitException ex = assertThrows(JobExitException.class, () -> context.failFinally("a message"));
        assertEquals("a message", ex.getMessage());
        assertFalse(ex.isSuccess());
        assertFalse(ex.isRetry());
        assertNull(ex.getRetryInSeconds());
    }

    @Test
    public void testRetry() {
        JobContext context = prepareContext();
        JobExitException ex = assertThrows(JobExitException.class, () -> context.retry("a message", 123));
        assertEquals("a message", ex.getMessage());
        assertFalse(ex.isSuccess());
        assertTrue(ex.isRetry());
        assertEquals(123, ex.getRetryInSeconds());
    }

    @Test
    public void testRequeue() {
        JobContext context = prepareContext();
        JobExitException ex = assertThrows(JobExitException.class, () -> context.requeue(123));
        assertNull(ex.getMessage());
        assertTrue(ex.isSuccess());
        assertTrue(ex.isRetry());
        assertEquals(123, ex.getRetryInSeconds());
    }

    @Test
    public void testComplete() {
        JobContext context = prepareContext();
        JobExitException ex = assertThrows(JobExitException.class, () -> context.complete("a message"));
        assertEquals("a message", ex.getMessage());
        assertTrue(ex.isSuccess());
        assertFalse(ex.isRetry());
        assertNull(ex.getRetryInSeconds());
    }

    @Test
    public void testCompleteWithoutMessage() {
        JobContext context = prepareContext();
        JobExitException ex = assertThrows(JobExitException.class, context::complete);
        assertNull(ex.getMessage());
        assertTrue(ex.isSuccess());
        assertFalse(ex.isRetry());
        assertNull(ex.getRetryInSeconds());
    }

}
