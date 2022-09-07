package org.javawebstack.jobs.storage;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.model.*;

import java.util.List;
import java.util.UUID;

public interface JobStorage {

    void createJob(JobInfo info, String payload);
    JobInfo getJob(UUID id);
    String getJobPayload(UUID id);
    void setJobStatus(UUID id, JobStatus status);
    void deleteJob(UUID id);
    List<JobInfo> queryJobs(JobQuery query);
    void createEvent(JobEvent data);
    JobEvent getEvent(UUID id);
    void createLogEntry(JobLogEntry entry);
    JobLogEntry getLogEntry(UUID eventId, UUID id);
    void createWorker(JobWorkerInfo info);
    JobWorkerInfo getWorker(UUID id);
    List<JobWorkerInfo> queryWorkers();
    void setWorkerOnline(UUID id, boolean online);

}
