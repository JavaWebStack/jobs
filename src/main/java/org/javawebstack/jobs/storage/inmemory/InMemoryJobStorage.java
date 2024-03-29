package org.javawebstack.jobs.storage.inmemory;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryJobStorage implements JobStorage {

    final List<JobInfo> jobs = new ArrayList<>();
    final Map<UUID, String> jobPayloads = new HashMap<>();
    final List<RecurringJobInfo> recurringJobs = new ArrayList<>();
    final List<JobEvent> events = new ArrayList<>();
    final Map<UUID, List<JobLogEntry>> logEntries = new HashMap<>();

    final List<JobWorkerInfo> workers = new ArrayList<>();

    public void createJob(JobInfo info, String payload) {
        JobStorage.super.createJob(info, payload);

        jobs.add(info.clone());
        jobPayloads.put(info.getId(), payload);
    }

    public boolean createRecurringJob(RecurringJobInfo info) {
        JobStorage.super.createRecurringJob(info);

        if (recurringJobs.stream().anyMatch(r -> r.getType().equals(info.getType()) && r.getQueue().equals(info.getQueue()) && r.getPayload().equals(info.getPayload()) && r.getCron().equals(info.getCron())))
            return false;

        return recurringJobs.add(info.clone());
    }

    public JobInfo getJob(UUID id) {
        return jobs.stream().filter(j -> j.getId().equals(id)).findFirst().map(JobInfo::clone).orElse(null);
    }

    public RecurringJobInfo getRecurringJob(UUID id) {
        return recurringJobs.stream().filter(j -> j.getId().equals(id)).findFirst().map(RecurringJobInfo::clone).orElse(null);
    }

    public String getJobPayload(UUID id) {
        return jobPayloads.get(id);
    }

    public void setJobStatus(UUID id, JobStatus status) {
        JobInfo info = jobs.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if(info == null)
            return;
        info.setStatus(status);
    }

    public void deleteJob(UUID id) {
        JobInfo info = jobs.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if(info == null)
            return;
        jobs.remove(info);
        jobPayloads.remove(info.getId());
    }

    public void deleteRecurringJob(UUID id) {
        RecurringJobInfo info = recurringJobs.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if (info == null)
            return;
        recurringJobs.remove(info);
    }

    public List<JobInfo> queryJobs(JobQuery query) {
        Stream<JobInfo> stream = jobs.stream();
        if(query.getType() != null)
            stream = stream.filter(j -> j.getType().equals(query.getType()));
        if(query.getStatus() != null) {
            List<JobStatus> statuses = Arrays.asList(query.getStatus());
            stream = stream.filter(j -> statuses.contains(j.getStatus()));
        }
        if(query.getOffset() != -1)
            stream = stream.skip(query.getOffset());
        if(query.getLimit() != -1)
            stream = stream.limit(query.getLimit());
        return stream.map(JobInfo::clone).collect(Collectors.toList());
    }

    public List<RecurringJobInfo> queryRecurringJobs(RecurringJobQuery query) {
        Stream<RecurringJobInfo> stream = recurringJobs.stream();
        if (query.getQueue() != null)
            stream = stream.filter(j -> j.getQueue().equals(query.getQueue()));
        if (query.getSinceLastExecution() != null)
            stream = stream.filter(j -> j.getLastExecutionAt() != null && j.getLastExecutionAt().before(query.getSinceLastExecution()));
        if(query.getType() != null)
            stream = stream.filter(j -> j.getType().equals(query.getType()));
        if(query.getOffset() != 0)
            stream = stream.skip(query.getOffset());
        if(query.getLimit() != -1)
            stream = stream.limit(query.getLimit());
        return stream.map(RecurringJobInfo::clone).collect(Collectors.toList());
    }

    public Map<JobStatus, Integer> getJobCountsByStatuses() {
        Map<JobStatus, Integer> counts = new HashMap<>();
        for(JobStatus status : JobStatus.values()) {
            counts.put(status, (int) jobs.stream().filter(r -> r.getStatus() == status).count());
        }
        return counts;
    }

    public void createEvent(JobEvent data) {
        data.checkRequired();
        data.sanitize();
        events.add(data.clone());
    }

    public JobEvent getEvent(UUID id) {
        return events.stream().filter(j -> j.getId().equals(id)).findFirst().map(JobEvent::clone).orElse(null);
    }

    public List<JobEvent> queryEvents(UUID jobId) {
        return events.stream().filter(e -> e.getJobId().equals(jobId)).map(JobEvent::clone).collect(Collectors.toList());
    }

    public void createLogEntry(JobLogEntry entry) {
        entry.checkRequired();
        entry.sanitize();
        List<JobLogEntry> entries = logEntries.computeIfAbsent(entry.getEventId(), k -> new ArrayList<>());
        entries.add(entry.clone());
    }

    public JobLogEntry getLogEntry(UUID eventId, UUID id) {
        List<JobLogEntry> entries = logEntries.get(eventId);
        if(entries == null)
            return null;
        return entries.stream().filter(j -> j.getId().equals(id)).findFirst().map(JobLogEntry::clone).orElse(null);
    }

    public List<JobLogEntry> queryLogEntries(UUID eventId) {
        List<JobLogEntry> entries = logEntries.get(eventId);
        if(entries == null)
            entries = new ArrayList<>();
        return new ArrayList<>(entries.stream().map(JobLogEntry::clone).collect(Collectors.toList()));
    }

    public void createWorker(JobWorkerInfo info) {
        info.checkRequired();
        info.sanitize();
        workers.add(info.clone());
    }

    public JobWorkerInfo getWorker(UUID id) {
        return workers.stream().filter(j -> j.getId().equals(id)).findFirst().map(JobWorkerInfo::clone).orElse(null);
    }

    public List<JobWorkerInfo> queryWorkers() {
        return workers.stream().map(JobWorkerInfo::clone).collect(Collectors.toList());
    }

    public void markOfflineWorkers() {
        workers.stream()
                .filter(JobWorkerInfo::isOnline)
                .filter(w -> w.getLastHeartbeatAt().before(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES))))
                .forEach(w -> w.setOnline(false));
    }

    public void deleteOfflineWorkers() {
        workers.removeIf(w -> w.isOffline() && w.getLastHeartbeatAt().before(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES))));
    }

    public void setWorkerOnline(UUID id, boolean online) {
        JobWorkerInfo info = workers.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if(info == null)
            return;
        info.setOnline(online);
        info.setLastHeartbeatAt(Date.from(Instant.now()));
    }

    public void updateRecurringJob(UUID id, UUID newJobId, Date lastExecutionAt) {
        RecurringJobInfo info = recurringJobs.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if (info == null)
            return;
        info.setLastJobId(newJobId);
        info.setLastExecutionAt(lastExecutionAt);
    }

}
