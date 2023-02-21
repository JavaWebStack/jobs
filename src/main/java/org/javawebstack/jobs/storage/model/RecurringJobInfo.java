package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.jobs.scheduler.interval.CronInterval;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
public class RecurringJobInfo {

    UUID id;
    UUID lastJobId;
    String queue;
    String payload;
    String type;
    CronInterval cron;
    Date lastExecutionAt;
    Date createdAt;

    public RecurringJobInfo clone() {
        return new RecurringJobInfo()
                .setId(id)
                .setLastJobId(lastJobId)
                .setQueue(queue)
                .setPayload(payload)
                .setType(type)
                .setCron(cron)
                .setLastExecutionAt(lastExecutionAt)
                .setCreatedAt(createdAt);
    }

    public void checkRequired() throws IllegalArgumentException {
        if (type == null)
            throw new IllegalArgumentException("Recurring Job type is required");
        if (cron == null)
            throw new IllegalArgumentException("Recurring Job cron is required");
    }

    public void sanitize() {
        if (id == null)
            id = UUID.randomUUID();
        if (createdAt == null)
            createdAt = new Date();
        if (lastExecutionAt == null)
            lastExecutionAt = new Date();
        if (payload == null)
            payload = "{}";
    }
}
