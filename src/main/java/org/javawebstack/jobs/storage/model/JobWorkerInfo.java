package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobWorkerInfo {

    UUID id;
    String queue;
    String hostname;
    boolean online;
    Date lastHeartbeatAt;
    Date createdAt;

    public JobWorkerInfo clone() {
        return new JobWorkerInfo()
                .setId(id)
                .setQueue(queue)
                .setHostname(hostname)
                .setOnline(online)
                .setLastHeartbeatAt(lastHeartbeatAt)
                .setCreatedAt(createdAt);
    }

}
