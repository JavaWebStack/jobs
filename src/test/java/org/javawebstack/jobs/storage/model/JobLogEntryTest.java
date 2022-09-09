package org.javawebstack.jobs.storage.model;

import org.javawebstack.jobs.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JobLogEntryTest {

    @Test
    public void testClone() {
        JobLogEntry entry = new JobLogEntry()
                .setId(UUID.randomUUID())
                .setEventId(UUID.randomUUID())
                .setLevel(LogLevel.WARNING)
                .setMessage("a message")
                .setCreatedAt(Date.from(Instant.now()));
        JobLogEntry clone = entry.clone();
        assertEquals(entry.getId(), clone.getId());
        assertEquals(entry.getEventId(), clone.getEventId());
        assertEquals(entry.getLevel(), clone.getLevel());
        assertEquals(entry.getMessage(), clone.getMessage());
        assertEquals(entry.getCreatedAt(), clone.getCreatedAt());
    }

    @Test
    public void testCheckRequired() {
        JobLogEntry entryWithoutEventId = new JobLogEntry()
                .setLevel(LogLevel.WARNING)
                .setMessage("a message");
        JobLogEntry entryWithoutLevel = new JobLogEntry()
                .setEventId(UUID.randomUUID())
                .setMessage("a message");
        JobLogEntry entryWithoutMessage = new JobLogEntry()
                .setEventId(UUID.randomUUID())
                .setLevel(LogLevel.WARNING);
        JobLogEntry entryWithRequired = new JobLogEntry()
                .setEventId(UUID.randomUUID())
                .setLevel(LogLevel.WARNING)
                .setMessage("a message");
        assertThrows(IllegalArgumentException.class, entryWithoutEventId::checkRequired);
        assertThrows(IllegalArgumentException.class, entryWithoutLevel::checkRequired);
        assertThrows(IllegalArgumentException.class, entryWithoutMessage::checkRequired);
        assertDoesNotThrow(entryWithRequired::checkRequired);
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
