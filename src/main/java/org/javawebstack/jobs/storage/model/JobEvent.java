package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.LogLevel;
import org.javawebstack.jobs.storage.JobStorage;

import java.text.SimpleDateFormat;
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
        SCHEDULED,
        ENQUEUED,
        PROCESSING,
        FAILED,
        SUCCESS
    }

    public static JobEvent createEnqueued(JobStorage storage, UUID jobId, String queue) {
        JobEvent event = new JobEvent()
                .setJobId(jobId)
                .setType(JobEvent.Type.ENQUEUED);
        storage.createEvent(event);
        storage.createLogEntry(new JobLogEntry()
                .setEventId(event.getId())
                .setLevel(LogLevel.INFO)
                .setMessage("Enqueued on queue '" + queue + "'")
        );
        return event;
    }

    public static JobEvent createScheduled(JobStorage storage, UUID jobId, Date at, String queue) {
        JobEvent event = new JobEvent()
                .setJobId(jobId)
                .setType(JobEvent.Type.SCHEDULED);
        storage.createEvent(event);
        storage.createLogEntry(new JobLogEntry()
                .setEventId(event.getId())
                .setLevel(LogLevel.INFO)
                .setMessage("Scheduled for '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(at) + "' on queue '" + queue + "'")
        );
        return event;
    }

}
