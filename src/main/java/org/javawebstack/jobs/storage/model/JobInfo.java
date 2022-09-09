package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.JobStatus;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobInfo {

    UUID id;
    JobStatus status;
    String type;
    int retries;
    int maxRetries;
    Date createdAt;

    public JobInfo clone() {
        return new JobInfo()
                .setId(id)
                .setStatus(status)
                .setType(type)
                .setCreatedAt(createdAt);
    }

    public void checkRequired() throws IllegalArgumentException {
        if(type == null)
            throw new IllegalArgumentException("Job type is required");
    }

    public void sanitize() {
        if(id == null)
            id = UUID.randomUUID();
        if(status == null)
            status = JobStatus.CREATED;
        if(createdAt == null)
            createdAt = Date.from(Instant.now());
    }

}
