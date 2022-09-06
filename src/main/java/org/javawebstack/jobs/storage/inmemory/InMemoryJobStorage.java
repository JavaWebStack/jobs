package org.javawebstack.jobs.storage.inmemory;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryJobStorage implements JobStorage {

    List<JobInfo> jobs = new ArrayList<>();
    Map<UUID, String> jobPayloads = new HashMap<>();
    List<JobEvent> events = new ArrayList<>();
    Map<UUID, List<JobLogEntry>> logEntries = new HashMap<>();

    List<JobWorkerInfo> workers = new ArrayList<>();

    public void createJob(JobInfo info, String payload) {
        if(info.getId() == null)
            info.setId(UUID.randomUUID());
        if(info.getCreatedAt() == null)
            info.setCreatedAt(Date.from(Instant.now()));
        if(info.getStatus() == null)
            info.setStatus(JobStatus.CREATED);
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

    public void createEvent(JobEvent data) {
        if(data.getId() == null)
            data.setId(UUID.randomUUID());
        if(data.getCreatedAt() == null)
            data.setCreatedAt(Date.from(Instant.now()));
        events.add(data.clone());
    }

    public void createLogEntry(JobLogEntry entry) {
        if(entry.getId() == null)
            entry.setId(UUID.randomUUID());
        if(entry.getCreatedAt() == null)
            entry.setCreatedAt(Date.from(Instant.now()));
        List<JobLogEntry> entries = logEntries.computeIfAbsent(entry.getEventId(), k -> new ArrayList<>());
        entries.add(entry.clone());
    }

    public void createWorker(JobWorkerInfo info) {
        if(info.getId() == null)
            info.setId(UUID.randomUUID());
        if(info.getCreatedAt() == null)
            info.setCreatedAt(Date.from(Instant.now()));
        if(info.getLastHeartbeatAt() == null)
            info.setLastHeartbeatAt(Date.from(Instant.now()));
        workers.add(info.clone());
    }

    public void setWorkerOnline(UUID id, boolean online) {
        JobWorkerInfo info = workers.stream().filter(j -> j.getId().equals(id)).findFirst().orElse(null);
        if(info == null)
            return;
        info.setOnline(online);
        info.setLastHeartbeatAt(Date.from(Instant.now()));
    }

}
