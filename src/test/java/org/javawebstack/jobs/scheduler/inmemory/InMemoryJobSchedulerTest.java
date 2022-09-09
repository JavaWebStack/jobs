package org.javawebstack.jobs.scheduler.inmemory;

import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.JobSchedulerTest;
import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;

public class InMemoryJobSchedulerTest extends JobSchedulerTest {

    protected JobScheduler initScheduler() {
        return new InMemoryJobScheduler();
    }

}
