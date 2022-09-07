package org.javawebstack.jobs.scheduler;

import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface JobScheduler {

    void enqueue(String queue, UUID id);
    void schedule(String queue, Date at, UUID id);
    UUID poll(String queue);
    List<UUID> processSchedule(String queue);
    List<JobScheduleEntry> getScheduleEntries(String queue);
    List<UUID> getQueueEntries(String queue);

}
