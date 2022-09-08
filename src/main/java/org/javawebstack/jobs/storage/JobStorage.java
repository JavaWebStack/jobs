package org.javawebstack.jobs.storage;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JobStorage {

    void createJob(JobInfo info, String payload);
    JobInfo getJob(UUID id);
    String getJobPayload(UUID id);
    void setJobStatus(UUID id, JobStatus status);
    void deleteJob(UUID id);
    List<JobInfo> queryJobs(JobQuery query);
    Map<JobStatus, Integer> getJobCountsByStatuses();
    void createEvent(JobEvent data);
    JobEvent getEvent(UUID id);
    List<JobEvent> queryEvents(UUID jobId);
    void createLogEntry(JobLogEntry entry);
    JobLogEntry getLogEntry(UUID eventId, UUID id);
    List<JobLogEntry> queryLogEntries(UUID eventId);
    void createWorker(JobWorkerInfo info);
    JobWorkerInfo getWorker(UUID id);
    List<JobWorkerInfo> queryWorkers();
    void setWorkerOnline(UUID id, boolean online);

}
