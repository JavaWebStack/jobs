package org.javawebstack.jobs;

import org.javawebstack.jobs.storage.JobStorage;

public class Jobs {

    private JobStorage storage;

    public Jobs(JobStorage storage) {
        this.storage = storage;
    }

}
