package org.javawebstack.jobs;

import org.javawebstack.jobs.serialization.JobSerializer;
import org.javawebstack.jobs.storage.JobStorage;

public class Jobs {

    private JobStorage storage;
    private JobSerializer serializer;

    public Jobs(JobStorage storage, JobSerializer serializer) {
        this.storage = storage;
        this.serializer = serializer;
    }

}
