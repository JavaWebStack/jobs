package org.javawebstack.jobs.storage.sql;

import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.storage.model.*;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.util.MapBuilder;
import org.javawebstack.jobs.util.SQLUtil;
import org.javawebstack.orm.wrapper.MySQL;
import org.javawebstack.orm.wrapper.SQL;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SQLJobStorage implements JobStorage {

    final SQL sql;
    final String tablePrefix;

    public SQLJobStorage(String host, int port, String database, String username, String password, String tablePrefix) {
        this(new MySQL(host, port, database, username, password), tablePrefix);
    }

    public SQLJobStorage(SQL sql, String tablePrefix) {
        this.sql = sql;
        this.tablePrefix = tablePrefix;
        try {
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("jobs") + "` (`id` VARCHAR(36), `status` ENUM('CREATED', 'SCHEDULED', 'ENQUEUED', 'PROCESSING', 'SUCCESS', 'FAILED'), `type` VARCHAR(100) NOT NULL, `payload` LONGTEXT NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("job_events") + "` (`id` VARCHAR(36), `job_id` VARCHAR(36) NOT NULL, `type` ENUM('ENQUEUED','EXECUTION','FAILED','SUCCESS') NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("job_log_entries") + "` (`id` VARCHAR(36), `event_id` VARCHAR(36) NOT NULL,`level` ENUM('INFO','WARNING','ERROR') NOT NULL, `message` LONGTEXT NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("job_workers") + "` (`id` VARCHAR(36), `queue` VARCHAR(50) NOT NULL, `hostname` VARCHAR(50) NOT NULL, `online` TINYINT(1), `last_heartbeat_at` TIMESTAMP NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String table(String name) {
        return tablePrefix + name;
    }

    public void createJob(JobInfo info, String payload) {
        if(info.getId() == null)
            info.setId(UUID.randomUUID());
        if(info.getCreatedAt() == null)
            info.setCreatedAt(Date.from(Instant.now()));
        if(info.getStatus() == null)
            info.setStatus(JobStatus.CREATED);
        SQLUtil.insert(sql, "jobs", new MapBuilder<String, Object>()
                .set("id", info.getId())
                .set("status", info.getStatus())
                .set("type", info.getType())
                .set("payload", payload)
                .set("created_at", info.getCreatedAt())
                .build()
        );
    }

    public JobInfo getJob(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, "jobs", "`id`,`status`,`type`,`created_at`", "WHERE `id`=? LIMIT 1", id);
        if(results.size() == 0)
            return null;
        return buildJobInfo(results.get(0));
    }

    public String getJobPayload(UUID id) {
        List<Map<String, Object>> results = SQLUtil.select(sql, "jobs", "`payload`", "WHERE `id`=? LIMIT 1", id);
        if(results.size() == 0)
            return null;
        return (String) results.get(0).get("payload");
    }

    public void setJobStatus(UUID id, JobStatus status) {
        SQLUtil.update(sql, "jobs", new MapBuilder<String, Object>()
                .set("status", status)
                .build()
                ,"`id`=?", id);
    }

    public void deleteJob(UUID id) {
        SQLUtil.delete(sql, "job_log_entries", "EXISTS(SELECT `id` FROM `" + table("job_events") + "` WHERE `" + table("job_events") + "`.id=`" + table("job_log_entries") + "`.event_id AND `" + table("job_events") + "`.job_id=?)", id);
        SQLUtil.delete(sql, "job_events", "`job_id`=?", id);
        SQLUtil.delete(sql, "jobs", "`id`=?", id);
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
            sb.append("`status`=?");
            params.add(query.getStatus());
        }
        if(query.getLimit() != -1 || query.getOffset() != -1) {
            int offset = query.getOffset();
            if(offset < 0)
                offset = 0;
            int limit = query.getLimit();
            if(limit < 0)
                limit = Integer.MAX_VALUE;
            sb.append(" LIMIT ").append(offset - 1).append(",").append(limit);
        }
        return SQLUtil.select(sql, "jobs", "`id`,`status`,`type`,`created_at`", sb.toString().trim(), params.toArray())
                .stream()
                .map(this::buildJobInfo)
                .collect(Collectors.toList());
    }

    public void createEvent(JobEvent event) {
        if(event.getId() == null)
            event.setId(UUID.randomUUID());
        if(event.getCreatedAt() == null)
            event.setCreatedAt(Date.from(Instant.now()));
        SQLUtil.insert(sql, "job_events", new MapBuilder<String, Object>()
                .set("id", event.getId())
                .set("job_id", event.getJobId())
                .set("type", event.getType())
                .set("created_at", event.getCreatedAt())
                .build()
        );
    }

    public void createLogEntry(JobLogEntry entry) {
        if(entry.getId() == null)
            entry.setId(UUID.randomUUID());
        if(entry.getCreatedAt() == null)
            entry.setCreatedAt(Date.from(Instant.now()));
        SQLUtil.insert(sql, "job_log_entries", new MapBuilder<String, Object>()
                .set("id", entry.getId())
                .set("event_id", entry.getEventId())
                .set("level", entry.getLevel())
                .set("message", entry.getMessage())
                .set("created_at", entry.getCreatedAt())
                .build()
        );
    }

    public void createWorker(JobWorkerInfo info) {
        if(info.getId() == null)
            info.setId(UUID.randomUUID());
        if(info.getCreatedAt() == null)
            info.setCreatedAt(Date.from(Instant.now()));
        if(info.getLastHeartbeatAt() == null)
            info.setLastHeartbeatAt(Date.from(Instant.now()));
        SQLUtil.insert(sql, "job_workers", new MapBuilder<String, Object>()
                .set("id", info.getId())
                .set("queue", info.getQueue())
                .set("online", info.isOnline())
                .set("hostname", info.getHostname())
                .set("last_heartbeat_at", info.getLastHeartbeatAt())
                .set("created_at", info.getCreatedAt())
                .build()
        );
    }

    public void setWorkerOnline(UUID id, boolean online) {
        SQLUtil.update(sql, "job_workers", new MapBuilder<String, Object>()
                .set("online", online)
                .set("last_heartbeat_at", Date.from(Instant.now()))
                .build()
        ,"`id`=?", id);
    }

    private JobInfo buildJobInfo(Map<String, Object> values) {
        JobInfo info = new JobInfo()
                .setId(UUID.fromString((String) values.get("id")))
                .setStatus(JobStatus.valueOf((String) values.get("status")))
                .setType((String) values.get("type"))
                .setCreatedAt((Date) values.get("created_at"));
        return info;
    }

}
