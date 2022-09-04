package org.javawebstack.jobs;

import lombok.Getter;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.serialization.JobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.JobInfo;

import java.util.Date;
import java.util.UUID;

@Getter
public class Jobs {

    JobStorage storage;
    JobScheduler scheduler;
    JobSerializer serializer;

    public Jobs(JobStorage storage, JobScheduler scheduler, JobSerializer serializer) {
        this.storage = storage;
        this.scheduler = scheduler;
        this.serializer = serializer;
    }

    public UUID enqueue(String queue, Job job) {
        JobInfo info = new JobInfo()
                .setType(job.getClass().getName())
                .setStatus(JobStatus.ENQUEUED);
        String payload = serializer.serialize(job);
        this.storage.createJob(info, payload);
        this.scheduler.enqueue(queue, info.getId());
        return info.getId();
    }

    public UUID schedule(String queue, Date at, Job job) {
        JobInfo info = new JobInfo()
                .setType(job.getClass().getName())
                .setStatus(JobStatus.SCHEDULED);
        String payload = serializer.serialize(job);
        this.storage.createJob(info, payload);
        this.scheduler.schedule(queue, at, info.getId());
        return info.getId();
    }

}
