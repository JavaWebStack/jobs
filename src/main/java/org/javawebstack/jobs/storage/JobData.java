package org.javawebstack.jobs.storage;

import java.util.UUID;

public class JobData {

    private UUID id;
    private String type;
    private String data;
    private long availableAt;

    public UUID getId() {
        return id;
    }

    public JobData setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public JobData setType(String type) {
        this.type = type;
        return this;
    }

    public String getData() {
        return data;
    }

    public JobData setData(String data) {
        this.data = data;
        return this;
    }

    public long getAvailableAt() {
        return availableAt;
    }

    public JobData setAvailableAt(long availableAt) {
        this.availableAt = availableAt;
        return this;
    }

}
