package org.javawebstack.jobs.storage.sql;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.LogLevel;
import org.javawebstack.jobs.scheduler.interval.CronInterval;
import org.javawebstack.jobs.storage.model.*;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.util.MapBuilder;
import org.javawebstack.jobs.util.SQLUtil;
import org.javawebstack.orm.wrapper.SQL;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SQLJobStorage implements JobStorage {

    final SQL sql;
    final String tablePrefix;

    public SQLJobStorage(SQL sql, String tablePrefix) {
        if(tablePrefix == null)
            tablePrefix = "";
        this.sql = sql;
        this.tablePrefix = tablePrefix;
        try {
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("jobs") + "` (`id` VARCHAR(36), `ord` BIGINT, `status` ENUM('CREATED', 'SCHEDULED', 'ENQUEUED', 'PROCESSING', 'SUCCESS', 'FAILED', 'DELETED'), `type` VARCHAR(100) NOT NULL, `payload` LONGTEXT NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("job_events") + "` (`id` VARCHAR(36), `ord` BIGINT, `job_id` VARCHAR(36) NOT NULL, `type` ENUM('SCHEDULED','ENQUEUED','PROCESSING','FAILED','SUCCESS') NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("job_log_entries") + "` (`id` VARCHAR(36), `ord` BIGINT, `event_id` VARCHAR(36) NOT NULL,`level` ENUM('INFO','WARNING','ERROR') NOT NULL, `message` LONGTEXT NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("job_workers") + "` (`id` VARCHAR(36), `ord` BIGINT, `queue` VARCHAR(50) NOT NULL, `hostname` VARCHAR(50) NOT NULL, `threads` INT(11) NOT NULL, `online` TINYINT(1), `last_heartbeat_at` TIMESTAMP NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("recurring_jobs") + "` (`id` VARCHAR(36), `ord` BIGINT, `queue` VARCHAR(50) NOT NULL, `payload` VARCHAR(1000) DEFAULT '{}', `type` VARCHAR(100) NOT NULL, `last_job_id` VARCHAR(36), `cron_expression` VARCHAR(255) NOT NULL, `last_execution_at` TIMESTAMP, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String table(String name) {
        return tablePrefix + name;
    }

    public void createJob(JobInfo info, String payload) {
        JobStorage.super.createJob(info, payload);

        SQLUtil.insert(sql, table("jobs"), new MapBuilder<String, Object>()
                .set("id", info.getId())
                .set("ord", System.currentTimeMillis())
                .set("status", info.getStatus())
                .set("type", info.getType())
                .set("payload", payload)
                .set("created_at", info.getCreatedAt())
                .build()
        );
    }

    public boolean createRecurringJob(RecurringJobInfo info) {
        JobStorage.super.createRecurringJob(info);

        List<RecurringJobInfo> recurringJobs = queryRecurringJobs(new RecurringJobQuery().setQueue(info.getQueue()).setType(info.getType()));
        if (recurringJobs.stream().anyMatch(r -> r.getPayload().equals(info.getPayload()) && r.getCron().equals(info.getCron())))
            return false;

        SQLUtil.insert(sql, table("recurring_jobs"), new MapBuilder<String, Object>()
                .set("id", info.getId())
                .set("last_job_id", info.getLastJobId())
                .set("queue", info.getQueue())
                .set("payload", info.getPayload())
                .set("type", info.getType())
                .set("ord", System.currentTimeMillis())
                .set("cron_expression", info.getCron().serialize())
                .set("created_at", new Date())
                .set("last_execution_at", info.getLastExecutionAt())
                .build()
        );

        return true;
    }

    public JobInfo getJob(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, table("jobs"), "`id`,`status`,`type`,`created_at`", "WHERE `id`=? LIMIT 1", id);
        if(results.size() == 0)
            return null;
        return buildJobInfo(results.get(0));
    }

    public RecurringJobInfo getRecurringJob(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, table("recurring_jobs"), "`id`,`last_job_id`,`queue`,`payload`,`type`,`cron_expression`,`last_execution_at`,`created_at`", "WHERE `id`=? LIMIT 1", id);
        if (results.size() == 0)
            return null;
        return buildRecurringJobInfo(results.get(0));
    }

    public String getJobPayload(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, table("jobs"), "`payload`", "WHERE `id`=? LIMIT 1", id);
        if(results.size() == 0)
            return null;
        return (String) results.get(0).get("payload");
    }

    public void setJobStatus(UUID id, JobStatus status) {
        SQLUtil.update(sql, table("jobs"), new MapBuilder<String, Object>()
                .set("status", status)
                .build()
                ,"`id`=?", id);
    }

    public void deleteJob(UUID id) {
        SQLUtil.delete(sql, table("job_log_entries"), "EXISTS(SELECT `id` FROM `" + table("job_events") + "` WHERE `" + table("job_events") + "`.id=`" + table("job_log_entries") + "`.event_id AND `" + table("job_events") + "`.job_id=?)", id);
        SQLUtil.delete(sql, table("job_events"), "`job_id`=?", id);
        SQLUtil.delete(sql, table("jobs"), "`id`=?", id);
    }

    public void deleteRecurringJob(UUID id) {
        SQLUtil.delete(sql, table("recurring_jobs"), "`id`=?", id);
    }

    public List<JobInfo> queryJobs(JobQuery query) {
        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if(query.getType() != null) {
            sb.append("WHERE `type`=?");
            params.add(query.getType());
        }
        if(query.getStatus() != null) {
            if(sb.length() == 0)
                sb.append("WHERE ");
            else
                sb.append(" AND ");
            if(query.getStatus().length == 1) {
                sb.append("`status`=?");
                params.add(query.getStatus()[0]);
            } else {
                sb.append("`status` IN (").append(Stream.of(query.getStatus()).map(s -> "?").collect(Collectors.joining(","))).append(")");
                params.addAll(Arrays.asList(query.getStatus()));
            }
        }
        sb.append(" ORDER BY `ord` DESC");
        if(query.getLimit() != -1 || query.getOffset() != -1) {
            int offset = query.getOffset();
            if(offset < 0)
                offset = 0;
            int limit = query.getLimit();
            if(limit < 0)
                limit = Integer.MAX_VALUE;
            sb.append(" LIMIT ").append(offset).append(",").append(limit);
        }
        return SQLUtil.select(sql, table("jobs"), "`id`,`status`,`type`,`created_at`", sb.toString().trim(), params.toArray())
                .stream()
                .map(this::buildJobInfo)
                .collect(Collectors.toList());
    }

    public List<RecurringJobInfo> queryRecurringJobs(RecurringJobQuery query) {
        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if(query.getType() != null) {
            sb.append("WHERE `type`=?");
            params.add(query.getType());
        }
        if (query.getQueue() != null) {
            if(sb.length() == 0)
                sb.append("WHERE ");
            else
                sb.append(" AND ");
            sb.append("`queue`=?");
            params.add(query.getQueue());
        }
        if (query.getSinceLastExecution() != null) {
            if(sb.length() == 0)
                sb.append("WHERE ");
            else
                sb.append(" AND ");
            sb.append("(`last_execution_at` IS NULL OR `last_execution_at`<=?)");
            params.add(query.getSinceLastExecution());
        }
        sb.append(" ORDER BY `ord`");
        if(query.getLimit() != -1 || query.getOffset() != -1) {
            int offset = query.getOffset();
            if(offset < 0)
                offset = 0;
            int limit = query.getLimit();
            if(limit < 0)
                limit = Integer.MAX_VALUE;
            sb.append(" LIMIT ").append(offset).append(",").append(limit);
        }
        return SQLUtil.select(sql, table("recurring_jobs"), "`id`,`last_job_id`,`queue`,`payload`,`type`,`cron_expression`,`last_execution_at`,`created_at`", sb.toString().trim(), params.toArray())
                .stream()
                .map(this::buildRecurringJobInfo)
                .collect(Collectors.toList());
    }

    public Map<JobStatus, Integer> getJobCountsByStatuses() {
        Map<JobStatus, Integer> counts = new HashMap<>();
        List<Map<String, Object>> results = SQLUtil.select(sql, table("jobs"), "`status`,COUNT(`status`) AS `count`", "GROUP BY `status`");
        for(JobStatus status : JobStatus.values()) {
            counts.put(status, results.stream().filter(r -> r.get("status") != null && r.get("status").equals(status.name())).map(r -> ((Long) r.get("count")).intValue()).findFirst().orElse(0));
        }
        return counts;
    }

    public void createEvent(JobEvent event) {
        event.checkRequired();
        event.sanitize();
        SQLUtil.insert(sql, table("job_events"), new MapBuilder<String, Object>()
                .set("id", event.getId())
                .set("ord", System.currentTimeMillis())
                .set("job_id", event.getJobId())
                .set("type", event.getType())
                .set("created_at", event.getCreatedAt())
                .build()
        );
    }

    public JobEvent getEvent(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, table("job_events"), "`id`,`job_id`,`type`,`created_at`", "WHERE `id`=? LIMIT 1", id);
        if(results.size() == 0)
            return null;
        return buildJobEvent(results.get(0));
    }

    public List<JobEvent> queryEvents(UUID jobId) {
        return SQLUtil.select(sql, table("job_events"), "`id`,`job_id`,`type`,`created_at`", "WHERE `job_id`=? ORDER BY `ord`", jobId).stream().map(this::buildJobEvent).collect(Collectors.toList());
    }

    public void createLogEntry(JobLogEntry entry) {
        entry.checkRequired();
        entry.sanitize();
        SQLUtil.insert(sql, table("job_log_entries"), new MapBuilder<String, Object>()
                .set("id", entry.getId())
                .set("ord", System.currentTimeMillis())
                .set("event_id", entry.getEventId())
                .set("level", entry.getLevel())
                .set("message", entry.getMessage())
                .set("created_at", entry.getCreatedAt())
                .build()
        );
    }

    public JobLogEntry getLogEntry(UUID eventId, UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, table("job_log_entries"), "`id`,`event_id`,`level`,`message`,`created_at`", "WHERE `event_id`=? AND `id`=? LIMIT 1", eventId, id);
        if(results.size() == 0)
            return null;
        return buildJobLogEntry(results.get(0));
    }

    public List<JobLogEntry> queryLogEntries(UUID eventId) {
        return SQLUtil.select(sql, table("job_log_entries"), "`id`,`event_id`,`level`,`message`,`created_at`", "WHERE `event_id`=? ORDER BY `ord` ASC", eventId).stream().map(this::buildJobLogEntry).collect(Collectors.toList());
    }

    public void createWorker(JobWorkerInfo info) {
        info.checkRequired();
        info.sanitize();
        SQLUtil.insert(sql, table("job_workers"), new MapBuilder<String, Object>()
                .set("id", info.getId())
                .set("ord", System.currentTimeMillis())
                .set("queue", info.getQueue())
                .set("threads", info.getThreads())
                .set("online", info.isOnline())
                .set("hostname", info.getHostname())
                .set("last_heartbeat_at", info.getLastHeartbeatAt())
                .set("created_at", info.getCreatedAt())
                .build()
        );
    }

    public JobWorkerInfo getWorker(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, table("job_workers"), "`id`,`queue`,`hostname`,`threads`,`online`,`last_heartbeat_at`,`created_at`", "WHERE `id`=? LIMIT 1", id);
        if(results.size() == 0)
            return null;
        return buildJobWorkerInfo(results.get(0));
    }

    public List<JobWorkerInfo> queryWorkers() {
        return SQLUtil.select(sql, table("job_workers"), "`id`,`queue`,`hostname`,`threads`,`online`,`last_heartbeat_at`,`created_at`", null).stream().map(this::buildJobWorkerInfo).collect(Collectors.toList());
    }

    public void markOfflineWorkers() {
        SQLUtil.update(sql, table("job_workers"), new MapBuilder<String, Object>()
                .set("online", false)
                .build()
                , "`online`=? AND `last_heartbeat_at`<=?", true, Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
    }

    public void deleteOfflineWorkers() {
        SQLUtil.delete(sql, table("job_workers"), "`online`=? AND `last_heartbeat_at`<=?", false, Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)));
    }

    public void setWorkerOnline(UUID id, boolean online) {
        SQLUtil.update(sql, table("job_workers"), new MapBuilder<String, Object>()
                .set("online", online)
                .set("last_heartbeat_at", Date.from(Instant.now()))
                .build()
        ,"`id`=?", id);
    }

    public void updateRecurringJob(UUID id, UUID newJobId, Date lastExecutionAt) {
        SQLUtil.update(sql, table("recurring_jobs"), new MapBuilder<String, Object>()
                .set("last_job_id", newJobId)
                .set("last_execution_at", lastExecutionAt)
                .build()
        , "`id`=?", id);
    }

    private JobInfo buildJobInfo(Map<String, Object> values) {
        return new JobInfo()
                .setId(UUID.fromString((String) values.get("id")))
                .setStatus(JobStatus.valueOf((String) values.get("status")))
                .setType((String) values.get("type"))
                .setCreatedAt((Date) values.get("created_at"));
    }

    private JobEvent buildJobEvent(Map<String, Object> values) {
        return new JobEvent()
                .setId(UUID.fromString((String) values.get("id")))
                .setJobId(UUID.fromString((String) values.get("job_id")))
                .setType(JobEvent.Type.valueOf((String) values.get("type")))
                .setCreatedAt((Date) values.get("created_at"));
    }

    private JobLogEntry buildJobLogEntry(Map<String, Object> values) {
        return new JobLogEntry()
                .setId(UUID.fromString((String) values.get("id")))
                .setEventId(UUID.fromString((String) values.get("event_id")))
                .setLevel(LogLevel.valueOf((String) values.get("level")))
                .setMessage((String) values.get("message"))
                .setCreatedAt((Date) values.get("created_at"));
    }

    private JobWorkerInfo buildJobWorkerInfo(Map<String, Object> values) {
        return new JobWorkerInfo()
                .setId(UUID.fromString((String) values.get("id")))
                .setQueue((String) values.get("queue"))
                .setHostname((String) values.get("hostname"))
                .setThreads((Integer) values.get("threads"))
                .setOnline((Boolean) values.get("online"))
                .setLastHeartbeatAt((Date) values.get("last_heartbeat_at"))
                .setCreatedAt((Date) values.get("created_at"));
    }

    private RecurringJobInfo buildRecurringJobInfo(Map<String, Object> values) {
        String rawLastJobId = (String) values.get("last_job_id");

        return new RecurringJobInfo()
                .setId(UUID.fromString((String) values.get("id")))
                .setLastJobId(rawLastJobId == null ? null : UUID.fromString(rawLastJobId))
                .setType((String) values.get("type"))
                .setQueue((String) values.get("queue"))
                .setPayload((String) values.get("payload"))
                .setCron(new CronInterval((String) values.get("cron_expression")))
                .setLastExecutionAt((Date) values.get("last_execution_at"))
                .setCreatedAt((Date) values.get("created_at"));
    }
}
