package org.javawebstack.jobs.api.controller;

import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.storage.JobStorage;

public abstract class Controller {

    protected Jobs jobs;
    protected JobStorage storage;
    protected JobScheduler scheduler;

    public Controller(Jobs jobs) {
        this.jobs = jobs;
        this.storage = jobs.getStorage();
        this.scheduler = jobs.getScheduler();
    }

}
