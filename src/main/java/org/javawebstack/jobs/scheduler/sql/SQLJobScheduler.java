package org.javawebstack.jobs.scheduler.sql;

import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.javawebstack.jobs.util.MapBuilder;
import org.javawebstack.jobs.util.SQLUtil;
import org.javawebstack.orm.wrapper.MySQL;
import org.javawebstack.orm.wrapper.SQL;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SQLJobScheduler implements JobScheduler {

    final SQL sql;
    final String tablePrefix;

    public SQLJobScheduler(SQL sql, String tablePrefix) {
        if(tablePrefix == null)
            tablePrefix = "";
        this.sql = sql;
        this.tablePrefix = tablePrefix;
        try {
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("queued_jobs") + "` (`id` VARCHAR(36), `ord` BIGINT, `queue` VARCHAR(50) NOT NULL, `job_id` VARCHAR(36) NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("scheduled_jobs") + "` (`id` VARCHAR(36), `ord` BIGINT, `queue` VARCHAR(50) NOT NULL, `job_id` VARCHAR(36) NOT NULL, `scheduled_at` TIMESTAMP NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String table(String name) {
        return tablePrefix + name;
    }

    public void enqueue(String queue, UUID id) {
        SQLUtil.insert(sql, table("queued_jobs"), new MapBuilder<String, Object>()
                .set("id", UUID.randomUUID())
                .set("ord", System.currentTimeMillis())
                .set("queue", queue)
                .set("job_id", id)
                .set("created_at", Date.from(Instant.now()))
                .build()
        );
    }

    public void schedule(String queue, Date at, UUID id) {
        SQLUtil.insert(sql, table("scheduled_jobs"), new MapBuilder<String, Object>()
                .set("id", UUID.randomUUID())
                .set("ord", System.currentTimeMillis())
                .set("queue", queue)
                .set("job_id", id)
                .set("scheduled_at", at)
                .set("created_at", Date.from(Instant.now()))
                .build()
        );
    }

    public UUID poll(String queue) {
        Map<String, Object> e = SQLUtil.select(sql, table("queued_jobs"), "`id`,`job_id`", "WHERE `queue`=? ORDER BY `ord` LIMIT 1", queue).stream().findFirst().orElse(null);
        if(e == null)
            return null;
        SQLUtil.delete(sql, table("queued_jobs"), "`id`=?", e.get("id"));
        return UUID.fromString((String) e.get("job_id"));
    }

    public List<UUID> processSchedule(String queue) {
        List<UUID> enqueued = new ArrayList<>();
        SQLUtil.select(sql, table("scheduled_jobs"), "`id`,`job_id`", "WHERE `queue`=? AND `scheduled_at`<=? ORDER BY `ord`", queue, Date.from(Instant.now())).forEach(e -> {
            UUID jobId = UUID.fromString((String) e.get("job_id"));
            enqueue(queue, jobId);
            enqueued.add(jobId);
            SQLUtil.delete(sql, table("scheduled_jobs"), "`id`=?", e.get("id"));
        });
        return enqueued;
    }

    public List<JobScheduleEntry> getScheduleEntries(String queue) {
        return SQLUtil.select(sql, table("scheduled_jobs"), "`job_id`,`scheduled_at`", "WHERE `queue`=? ORDER BY `ord`", queue).stream().map(this::buildScheduleEntry).collect(Collectors.toList());
    }

    public List<UUID> getQueueEntries(String queue) {
        return SQLUtil.select(sql, table("queued_jobs"), "`job_id`", "WHERE `queue`=? ORDER BY `ord`", queue).stream().map(e -> UUID.fromString((String) e.get("job_id"))).collect(Collectors.toList());
    }

    private JobScheduleEntry buildScheduleEntry(Map<String, Object> values) {
        return new JobScheduleEntry()
                .setJobId(UUID.fromString((String) values.get("job_id")))
                .setAt((Date) values.get("scheduled_at"));
    }

}
