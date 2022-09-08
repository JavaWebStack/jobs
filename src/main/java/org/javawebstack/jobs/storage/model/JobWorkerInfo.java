package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobWorkerInfo {

    UUID id;
    String queue;
    String hostname;
    boolean online;
    int threads;
    Date lastHeartbeatAt;
    Date createdAt;

    public void checkRequired() throws IllegalArgumentException {
        if(queue == null)
            throw new IllegalArgumentException("Worker queue is required");
        if(hostname == null)
            throw new IllegalArgumentException("Worker hostname is required");
    }

    public void sanitize() {
        if(id == null)
            id = UUID.randomUUID();
        if(lastHeartbeatAt == null)
            lastHeartbeatAt = Date.from(Instant.now());
        if(createdAt == null)
            createdAt = Date.from(Instant.now());
    }

    public JobWorkerInfo clone() {
        return new JobWorkerInfo()
                .setId(id)
                .setQueue(queue)
                .setHostname(hostname)
                .setThreads(threads)
                .setOnline(online)
                .setLastHeartbeatAt(lastHeartbeatAt)
                .setCreatedAt(createdAt);
    }

}
