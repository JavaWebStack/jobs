package org.javawebstack.jobs.scheduler.inmemory;

import lombok.AllArgsConstructor;
import org.javawebstack.jobs.scheduler.JobScheduler;

import java.time.Instant;
import java.util.*;

public class InMemoryJobScheduler implements JobScheduler {

    Map<String, Queue<UUID>> queues = new HashMap<>();
    List<ScheduleEntry> schedule = new ArrayList<>();

    public void enqueue(String queue, UUID id) {
        Queue<UUID> q = queues.computeIfAbsent(queue, k -> new LinkedList<>());
        q.offer(id);
    }

    public void schedule(String queue, Date at, UUID id) {
        schedule.add(new ScheduleEntry(id, queue, at));
    }

    public synchronized void processSchedule() {
        Date now = Date.from(Instant.now());
        for(ScheduleEntry e : new ArrayList<>(schedule)) {
            if(e.at.before(now)) {
                schedule.remove(e);
                enqueue(e.queue, e.id);
            }
        }
    }

    public UUID poll(String queue) {
        Queue<UUID> q = queues.get(queue);
        if(q == null)
            return null;
        return q.poll();
    }

    @AllArgsConstructor
    private static class ScheduleEntry {

        UUID id;
        String queue;
        Date at;

    }

}
