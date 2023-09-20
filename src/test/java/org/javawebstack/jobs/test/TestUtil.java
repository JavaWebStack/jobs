package org.javawebstack.jobs.test;

import org.javawebstack.orm.connection.pool.PooledSQL;
import org.javawebstack.orm.connection.pool.SQLPool;

import java.sql.SQLException;

public class TestUtil {

    public static void purgeDatabase(SQLPool pool) {
        purgeStorageDatabase(pool);
        purgeSchedulerDatabase(pool);
    }

    public static void purgeStorageDatabase(SQLPool pool) {
        try(PooledSQL sql = pool.get()) {
            String[] TABLES = {
                    "jobs",
                    "job_events",
                    "job_log_entries",
                    "job_workers",
                    "recurring_jobs"
            };
            for(String t : TABLES)
                sql.write("DROP TABLE IF EXISTS `" + t + "`;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void purgeSchedulerDatabase(SQLPool pool) {
        try(PooledSQL sql = pool.get()) {
            String[] TABLES = {
                    "scheduled_jobs",
                    "queued_jobs"
            };
            for(String t : TABLES)
                sql.write("DROP TABLE IF EXISTS `" + t + "`;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
