package org.javawebstack.jobs.storage;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.model.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface JobStorage {

    default void createJob(JobInfo info, String payload) {
        info.checkRequired();
        info.sanitize();
    }
    default boolean createRecurringJob(RecurringJobInfo info) {
        info.checkRequired();
        info.sanitize();

        return false;
    }
    JobInfo getJob(UUID id);
    RecurringJobInfo getRecurringJob(UUID id);
    String getJobPayload(UUID id);
    void setJobStatus(UUID id, JobStatus status);
    void deleteJob(UUID id);
    void deleteRecurringJob(UUID id);
    List<JobInfo> queryJobs(JobQuery query);
    List<RecurringJobInfo> queryRecurringJobs(RecurringJobQuery query);
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
    void markOfflineWorkers();
    void deleteOfflineWorkers();
    void setWorkerOnline(UUID id, boolean online);
    void updateRecurringJob(UUID id, UUID newJobId, Date lastExecutionAt);

}
