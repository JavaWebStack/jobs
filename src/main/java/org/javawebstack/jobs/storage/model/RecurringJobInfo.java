package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.scheduler.interval.CronInterval;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class RecurringJobInfo {

    UUID jobId;
    CronInterval cron;
    Date lastExecutionAt;
    Date createdAt;

    public RecurringJobInfo clone() {
        return new RecurringJobInfo()
                .setJobId(jobId)
                .setCron(cron)
                .setLastExecutionAt(lastExecutionAt)
                .setCreatedAt(createdAt);
    }

    public void checkRequired() throws IllegalArgumentException {
        if (jobId == null)
            throw new IllegalArgumentException("Recurring Job jobId is required");
        if (cron == null)
            throw new IllegalArgumentException("Recurring Job cron is required");
    }

    public void sanitize() {
        if (createdAt == null)
            createdAt = new Date();
        if (lastExecutionAt == null)
            lastExecutionAt = cron.next(createdAt);
    }
}
