package org.javawebstack.jobs.scheduler;

import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;

public class InMemoryJobSchedulerTest extends JobSchedulerTest {

    protected JobScheduler initScheduler() {
        return new InMemoryJobScheduler();
    }

}
