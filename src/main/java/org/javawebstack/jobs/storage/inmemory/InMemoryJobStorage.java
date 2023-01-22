package org.javawebstack.jobs.storage.inmemory;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryJobStorage implements JobStorage {

    final List<JobInfo> jobs = new ArrayList<>();
    final Map<UUID, String> jobPayloads = new HashMap<>();
    final List<JobEvent> events = new ArrayList<>();
    final Map<UUID, List<JobLogEntry>> logEntries = new HashMap<>();

    final List<JobWorkerInfo> workers = new ArrayList<>();

    public void createJob(JobInfo info, String payload) {
        info.checkRequired();
        info.sanitize();
        jobs.add(info.clone());
        jobPayloads.put(info.getId(), payload);
    }

    public JobInfo getJob(UUID id) {
        return jobs.stream().filter(j -> j.getId().equals(id)).findFirst().map(JobInfo::clone).orElse(null);
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
        return entries.stream().map(JobLogEntry::clone).collect(Collectors.toList());
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

    public void setWorkerOnline(UUID id, boolean online) {
        JobWorkerInfo info = workers.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if(info == null)
            return;
        info.setOnline(online);
        info.setLastHeartbeatAt(Date.from(Instant.now()));
    }

}
