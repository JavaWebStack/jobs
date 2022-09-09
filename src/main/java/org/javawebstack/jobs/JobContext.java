package org.javawebstack.jobs;

import lombok.Getter;
import org.javawebstack.jobs.storage.model.JobEvent;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobLogEntry;
import org.javawebstack.jobs.util.JobExitException;

import java.util.Date;
import java.util.UUID;

public class JobContext {

    final Jobs jobs;
    final JobInfo info;
    @Getter
    final String queue;
    final JobEvent event;

    public JobContext(Jobs jobs, JobInfo info, String queue, JobEvent event) {
        this.jobs = jobs;
        this.info = info;
        this.queue = queue;
        this.event = event;
    }

    public UUID getId() {
        return info.getId();
    }

    public int getMaxRetries() {
        return info.getMaxRetries();
    }

    public int getCurrentRetry() {
        return info.getRetries();
    }

    public void enqueue(String queue) {
        jobs.enqueue(queue, info.getId());
    }

    public void schedule(String queue, Date at) {
        jobs.schedule(queue, at, info.getId());
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
        throw new JobExitException(false, message, true, null);
    }

    public void failFinally(String message) {
        throw new JobExitException(false, message, false, null);
    }

    public void retry(String message, int retryInSeconds) {
        throw new JobExitException(false, message, true, retryInSeconds);
    }

    public void requeue(int requeueInSeconds) {
        throw new JobExitException(true, null, true, requeueInSeconds);
    }

    public void complete() {
        complete(null);
    }

    public void complete(String message) {
        throw new JobExitException(true, message, false, null);
    }

}
