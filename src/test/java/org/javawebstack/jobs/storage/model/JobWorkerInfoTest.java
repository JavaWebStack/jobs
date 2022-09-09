package org.javawebstack.jobs.storage.model;

import org.javawebstack.jobs.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JobWorkerInfoTest {

    @Test
    public void testClone() {
        JobWorkerInfo info = new JobWorkerInfo()
                .setId(UUID.randomUUID())
                .setQueue("a queue")
                .setHostname("example.com")
                .setThreads(5)
                .setOnline(true)
                .setLastHeartbeatAt(Date.from(Instant.now()))
                .setCreatedAt(Date.from(Instant.now().minusSeconds(3600)));
        JobWorkerInfo clone = info.clone();
        assertEquals(info.getId(), clone.getId());
        assertEquals(info.getQueue(), clone.getQueue());
        assertEquals(info.getHostname(), clone.getHostname());
        assertEquals(info.getThreads(), clone.getThreads());
        assertEquals(info.isOnline(), clone.isOnline());
        assertEquals(info.getLastHeartbeatAt(), clone.getLastHeartbeatAt());
        assertEquals(info.getCreatedAt(), clone.getCreatedAt());
    }

    @Test
    public void testCheckRequired() {
        JobWorkerInfo infoWithoutQueue = new JobWorkerInfo()
                .setHostname("example.com");
        JobWorkerInfo infoWithoutHostname = new JobWorkerInfo()
                .setQueue("a queue");
        JobWorkerInfo infoWithRequired = new JobWorkerInfo()
                .setQueue("a queue")
                .setHostname("example.com");
        assertThrows(IllegalArgumentException.class, infoWithoutQueue::checkRequired);
        assertThrows(IllegalArgumentException.class, infoWithoutHostname::checkRequired);
        assertDoesNotThrow(infoWithRequired::checkRequired);
    }

    @Test
    public void testSanitizeDoesNotManipulateGivenFields() {
        UUID expectedId = UUID.randomUUID();
        UUID expectedEventId = UUID.randomUUID();
        LogLevel expectedLevel = LogLevel.WARNING;
        String expectedMessage = "a message";
        Date expectedCreatedAt = Date.from(Instant.now());

        JobLogEntry completeEntry = new JobLogEntry()
                .setId(expectedId)
                .setEventId(expectedEventId)
                .setLevel(expectedLevel)
                .setMessage(expectedMessage)
                .setCreatedAt(expectedCreatedAt);
        completeEntry.sanitize();

        assertEquals(expectedId, completeEntry.getId());
        assertEquals(expectedEventId, completeEntry.getEventId());
        assertEquals(expectedLevel, completeEntry.getLevel());
        assertEquals(expectedMessage, completeEntry.getMessage());
        assertEquals(expectedCreatedAt, completeEntry.getCreatedAt());
    }

    @Test
    public void testSanitizeAddsMissingFields() {
        JobLogEntry incompleteEntry = new JobLogEntry()
                .setEventId(UUID.randomUUID())
                .setLevel(LogLevel.WARNING)
                .setMessage("a message");
        incompleteEntry.sanitize();
        assertNotNull(incompleteEntry.getId());
        assertNotNull(incompleteEntry.getCreatedAt());
    }

}
