package org.javawebstack.jobs.scheduler.sql;

import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.util.MapBuilder;
import org.javawebstack.jobs.util.SQLUtil;
import org.javawebstack.orm.wrapper.MySQL;
import org.javawebstack.orm.wrapper.SQL;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class SQLJobScheduler implements JobScheduler {

    final SQL sql;
    final String tablePrefix;

    public SQLJobScheduler(String host, int port, String database, String username, String password, String tablePrefix) {
        this(new MySQL(host, port, database, username, password), tablePrefix);
    }

    public SQLJobScheduler(SQL sql, String tablePrefix) {
        this.sql = sql;
        this.tablePrefix = tablePrefix;
        try {
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("queued_jobs") + "` (`id` VARCHAR(36), `queue` VARCHAR(50) NOT NULL, `job_id` VARCHAR(36) NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + table("scheduled_jobs") + "` (`id` VARCHAR(36), `queue` VARCHAR(50) NOT NULL, `job_id` VARCHAR(36) NOT NULL, `scheduled_at` TIMESTAMP NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY(`id`));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String table(String name) {
        return tablePrefix + name;
    }

    public void enqueue(String queue, UUID id) {
        SQLUtil.insert(sql, "queued_jobs", new MapBuilder<String, Object>()
                .set("id", UUID.randomUUID())
                .set("queue", queue)
                .set("job_id", id)
                .set("created_at", Date.from(Instant.now()))
                .build()
        );
    }

    public void schedule(String queue, Date at, UUID id) {
        SQLUtil.insert(sql, "scheduled_jobs", new MapBuilder<String, Object>()
                .set("id", UUID.randomUUID())
                .set("queue", queue)
                .set("job_id", id)
                .set("scheduled_at", at)
                .set("created_at", Date.from(Instant.now()))
                .build()
        );
    }

    public UUID poll(String queue) {
        // TODO
        return null;
    }

}
