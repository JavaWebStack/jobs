package org.javawebstack.jobs.api.response;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.javawebstack.jobs.storage.model.JobInfo;

import java.util.Date;
import java.util.UUID;

public class JobResponse {

    UUID id;
    JobStatus status;
    String type;
    int retries;
    int maxRetries;
    Date createdAt;
    Date scheduledAt;

    public JobResponse(JobInfo job, JobScheduleEntry schedule) {
        this.id = job.getId();
        this.status = job.getStatus();
        this.type = job.getType();
        this.retries = job.getRetries();
        this.maxRetries = job.getMaxRetries();
        this.createdAt = job.getCreatedAt();
        if (schedule != null) {
            this.scheduledAt = schedule.getAt();
        }
    }

}
