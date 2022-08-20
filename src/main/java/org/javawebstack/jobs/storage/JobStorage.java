package org.javawebstack.jobs.storage;

import org.javawebstack.jobs.storage.model.JobData;
import org.javawebstack.jobs.storage.model.JobEventData;
import org.javawebstack.jobs.storage.model.JobLogData;
import org.javawebstack.jobs.storage.model.JobQuery;

import java.util.List;
import java.util.UUID;

public interface JobStorage {

    void createJob(JobData data);
    JobData getJob(UUID id);
    void deleteJob(UUID id);
    List<JobData> queryJobs(JobQuery query);
    void createEvent(JobEventData data);
    void createLog(JobLogData data);

}
