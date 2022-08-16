package org.javawebstack.jobs.storage;

import java.util.UUID;

public interface JobStorage {

    void createJob(String queue, long currentTime, JobData data);
    JobData pollJob(String queue, long currentTime);
    JobData getJob(UUID id);

}
