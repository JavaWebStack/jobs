package org.javawebstack.jobs.scheduler;

import java.util.Date;
import java.util.UUID;

public interface JobScheduler {

    void enqueue(String queue, UUID id);
    void schedule(String queue, Date at, UUID id);
    UUID poll(String queue);
    void processSchedule();

}
