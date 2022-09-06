package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

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

    public enum Type {
        ENQUEUED,
        EXECUTION,
        FAILED,
        SUCCESS
    }

}
