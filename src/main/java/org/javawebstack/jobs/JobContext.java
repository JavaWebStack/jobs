package org.javawebstack.jobs;

import org.javawebstack.jobs.storage.model.JobEvent;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobLogEntry;
import org.javawebstack.jobs.util.JobExitException;

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

    public void fail(String message) {
        throw new JobExitException(false, message, -1);
    }

    public void retry(String message, int retryInSeconds) {
        throw new JobExitException(false, message, retryInSeconds);
    }

    public void requeue(int requeueInSeconds) {
        throw new JobExitException(true, null, requeueInSeconds);
    }

    public void complete() {
        complete(null);
    }

    public void complete(String message) {
        throw new JobExitException(true, message, -1);
    }

}
