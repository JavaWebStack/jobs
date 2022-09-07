package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.LogLevel;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobLogEntry {

    UUID id;
    UUID eventId;
    LogLevel level;
    String message;
    Date createdAt;

    public void checkRequired() throws IllegalArgumentException {
        if(eventId == null)
            throw new IllegalArgumentException("Log Entry eventId is required");
        if(level == null)
            throw new IllegalArgumentException("Log Entry level is required");
        if(message == null)
            throw new IllegalArgumentException("Log Entry message is required");
    }

    public void sanitize() {
        if(id == null)
            id = UUID.randomUUID();
        if(createdAt == null)
            createdAt = Date.from(Instant.now());
    }

    public JobLogEntry clone() {
        return new JobLogEntry()
                .setId(id)
                .setEventId(eventId)
                .setLevel(level)
                .setMessage(message)
                .setCreatedAt(createdAt);
    }

}
