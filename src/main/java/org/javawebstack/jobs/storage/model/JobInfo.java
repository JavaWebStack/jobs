package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.JobStatus;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobInfo {

    UUID id;
    JobStatus status;
    String type;
    Date createdAt;

    public JobInfo clone() {
        return new JobInfo()
                .setId(id)
                .setStatus(status)
                .setType(type)
                .setCreatedAt(createdAt);
    }

}
