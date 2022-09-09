package org.javawebstack.jobs;

import lombok.Getter;
import org.javawebstack.abstractdata.mapper.Mapper;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.serialization.JobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.model.JobEvent;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobLogEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Getter
public class Jobs {

    final JobStorage storage;
    final JobScheduler scheduler;
    final JobSerializer serializer;

    public Jobs(JobStorage storage, JobScheduler scheduler, JobSerializer serializer) {
        this.storage = storage;
        this.scheduler = scheduler;
        this.serializer = serializer;
    }

    public UUID enqueue(String queue, Job job) {
        return enqueue(queue, job.getClass().getName(), serializer.serialize(job));
    }

    public UUID enqueue(String queue, String type, String payload) {
        JobInfo info = new JobInfo()
                .setType(type)
                .setStatus(JobStatus.ENQUEUED);
        this.storage.createJob(info, payload);
        this.scheduler.enqueue(queue, info.getId());
        JobEvent.createEnqueued(storage, info.getId(), queue);
        return info.getId();
    }

    public UUID schedule(String queue, Date at, Job job) {
        return schedule(queue, at, job.getClass().getName(), serializer.serialize(job));
    }

    public UUID schedule(String queue, Date at, String type, String payload) {
        JobInfo info = new JobInfo()
                .setType(type)
                .setStatus(JobStatus.SCHEDULED);
        this.storage.createJob(info, payload);
        this.scheduler.schedule(queue, at, info.getId());
        JobEvent.createScheduled(storage, info.getId(), at, queue);
        return info.getId();
    }

}
