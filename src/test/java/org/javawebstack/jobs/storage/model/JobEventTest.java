package org.javawebstack.jobs.storage.model;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.LogLevel;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.test.jobs.NoOpJob;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class JobEventTest {

    @Test
    public void testClone() {
        JobEvent event = new JobEvent()
                .setId(UUID.randomUUID())
                .setJobId(UUID.randomUUID())
                .setType(JobEvent.Type.SCHEDULED)
                .setCreatedAt(Date.from(Instant.now()));
        JobEvent clone = event.clone();
        assertEquals(event.getId(), clone.getId());
        assertEquals(event.getJobId(), clone.getJobId());
        assertEquals(event.getType(), clone.getType());
        assertEquals(event.getCreatedAt(), clone.getCreatedAt());
    }

    @Test
    public void testCheckRequired() {
        JobEvent eventWithoutJobId = new JobEvent()
                .setType(JobEvent.Type.SCHEDULED);
        JobEvent eventWithoutType = new JobEvent()
                .setJobId(UUID.randomUUID());
        JobEvent eventWithRequired = new JobEvent()
                .setJobId(UUID.randomUUID())
                .setType(JobEvent.Type.SCHEDULED);
        assertThrows(IllegalArgumentException.class, eventWithoutJobId::checkRequired);
        assertThrows(IllegalArgumentException.class, eventWithoutType::checkRequired);
        assertDoesNotThrow(eventWithRequired::checkRequired);
    }

    @Test
    public void testSanitizeDoesNotManipulateGivenFields() {
        UUID expectedId = UUID.randomUUID();
        UUID expectedJobId = UUID.randomUUID();
        JobEvent.Type expectedType = JobEvent.Type.SCHEDULED;
        Date expectedCreatedAt = Date.from(Instant.now());

        JobEvent completeEvent = new JobEvent()
                .setId(expectedId)
                .setJobId(expectedJobId)
                .setType(expectedType)
                .setCreatedAt(expectedCreatedAt);
        completeEvent.sanitize();

        assertEquals(expectedId, completeEvent.getId());
        assertEquals(expectedJobId, completeEvent.getJobId());
        assertEquals(expectedType, completeEvent.getType());
        assertEquals(expectedCreatedAt, completeEvent.getCreatedAt());
    }

    @Test
    public void testSanitizeAddsMissingFields() {
        JobEvent incompleteInfo = new JobEvent()
                .setJobId(UUID.randomUUID())
                .setType(JobEvent.Type.PROCESSING);
        incompleteInfo.sanitize();
        assertNotNull(incompleteInfo.getId());
        assertNotNull(incompleteInfo.getCreatedAt());
    }

    @Test
    public void testCreateEnqueued() {
        JobStorage storage = new InMemoryJobStorage();
        UUID expectedJobId = UUID.randomUUID();
        JobEvent event = JobEvent.createEnqueued(storage, expectedJobId, "test");
        assertNotNull(event);
        assertNotNull(event.getId());
        JobEvent createdEvent = storage.getEvent(event.getId());
        assertNotNull(createdEvent);
        assertEquals(expectedJobId, createdEvent.getJobId());
        assertEquals(JobEvent.Type.ENQUEUED, createdEvent.getType());
        List<JobLogEntry> logEntries = storage.queryLogEntries(event.getId());
        assertEquals(1, logEntries.size());
        JobLogEntry entry = logEntries.get(0);
        assertEquals(LogLevel.INFO, entry.getLevel());
        assertEquals("Enqueued on queue 'test'", entry.getMessage());
    }

    @Test
    public void testCreateScheduled() {
        JobStorage storage = new InMemoryJobStorage();
        UUID expectedJobId = UUID.randomUUID();
        Date expectedAt = Date.from(Instant.now().plusSeconds(3600));
        JobEvent event = JobEvent.createScheduled(storage, expectedJobId, expectedAt, "test");
        assertNotNull(event);
        assertNotNull(event.getId());
        JobEvent createdEvent = storage.getEvent(event.getId());
        assertNotNull(createdEvent);
        assertEquals(expectedJobId, createdEvent.getJobId());
        assertEquals(JobEvent.Type.SCHEDULED, createdEvent.getType());
        List<JobLogEntry> logEntries = storage.queryLogEntries(event.getId());
        assertEquals(1, logEntries.size());
        JobLogEntry entry = logEntries.get(0);
        assertEquals(LogLevel.INFO, entry.getLevel());
        Pattern messagePattern = Pattern.compile("Scheduled for '[2-9][0-9]{3}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}' on queue 'test'");
        assertTrue(messagePattern.matcher(entry.getMessage()).matches());
    }

}
