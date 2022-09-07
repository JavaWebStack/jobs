package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobEvent {

    UUID id;
    UUID jobId;
    Type type;
    Date createdAt;

    public JobEvent clone() {
        return new JobEvent()
                .setId(id)
                .setJobId(jobId)
                .setType(type)
                .setCreatedAt(createdAt);
    }

    public void checkRequired() throws IllegalArgumentException {
        if(jobId == null)
            throw new IllegalArgumentException("Event jobId is required");
        if(type == null)
            throw new IllegalArgumentException("Event type is required");
    }

    public void sanitize() {
        if(id == null)
            id = UUID.randomUUID();
        if(createdAt == null)
            createdAt = Date.from(Instant.now());
    }

    public enum Type {
        ENQUEUED,
        EXECUTION,
        FAILED,
        SUCCESS
    }

}
