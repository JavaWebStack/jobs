package org.javawebstack.jobs;

import org.javawebstack.jobs.storage.model.JobEvent;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobLogEntry;

import java.util.Date;
import java.util.UUID;

public class JobContext {

    final Jobs jobs;
    final JobInfo info;
    final JobEvent event;

    public JobContext(Jobs jobs, JobInfo info, JobEvent event) {
        this.jobs = jobs;
        this.info = info;
        this.event = event;
    }

    public UUID getId() {
        return info.getId();
    }

    public void enqueue(String queue) {
        jobs.getScheduler().enqueue(queue, info.getId());
    }

    public void schedule(String queue, Date at) {
        jobs.getScheduler().schedule(queue, at, info.getId());
    }

    public void info(String message) {
        jobs.getStorage().createLogEntry(new JobLogEntry().setLevel(LogLevel.INFO).setEventId(event.getId()).setMessage(message));
    }

    public void warning(String message) {
        jobs.getStorage().createLogEntry(new JobLogEntry().setLevel(LogLevel.WARNING).setEventId(event.getId()).setMessage(message));
    }

    public void error(String message) {
        jobs.getStorage().createLogEntry(new JobLogEntry().setLevel(LogLevel.ERROR).setEventId(event.getId()).setMessage(message));
    }

}
