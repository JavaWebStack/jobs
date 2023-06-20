package org.javawebstack.jobs.scheduler.inmemory;

import lombok.AllArgsConstructor;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryJobScheduler implements JobScheduler {

    final Map<String, Queue<UUID>> queues = new HashMap<>();
    final List<ScheduleEntry> schedule = new ArrayList<>();

    public void enqueue(String queue, UUID id) {
        Queue<UUID> q = queues.computeIfAbsent(queue, k -> new LinkedList<>());
        q.offer(id);
    }

    public void dequeue(UUID id) {
        queues.values().forEach(q -> q.remove(id));
    }

    public void schedule(String queue, Date at, UUID id) {
        schedule.add(new ScheduleEntry(id, queue, at));
    }

    public synchronized List<UUID> processSchedule(String queue) {
        List<UUID> enqueued = new ArrayList<>();
        Date now = Date.from(Instant.now());
        for(ScheduleEntry e : schedule.stream().filter(e -> e.queue.equals(queue)).collect(Collectors.toList())) {
            if(e.at.before(now)) {
                schedule.remove(e);
                enqueue(e.queue, e.id);
                enqueued.add(e.id);
            }
        }
        return enqueued;
    }

    public List<JobScheduleEntry> getScheduleEntries(String queue) {
        return schedule.stream().filter(e -> e.queue.equals(queue)).map(e -> new JobScheduleEntry().setJobId(e.id).setAt(e.at)).collect(Collectors.toList());
    }

    public List<UUID> getQueueEntries(String queue) {
        if(!queues.containsKey(queue))
            return new ArrayList<>();
        return new ArrayList<>(queues.get(queue));
    }

    public synchronized UUID poll(String queue) {
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
