package org.javawebstack.jobs.test;

import org.javawebstack.orm.wrapper.SQL;

import java.sql.SQLException;

public class TestUtil {

    public static void purgeDatabase(SQL sql) {
        purgeStorageDatabase(sql);
        purgeSchedulerDatabase(sql);
    }

    public static void purgeStorageDatabase(SQL sql) {
        try {
            String[] TABLES = {
                    "jobs",
                    "job_events",
                    "job_log_entries",
                    "job_workers"
            };
            for(String t : TABLES)
                sql.write("DROP TABLE IF EXISTS `" + t + "`;");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void purgeSchedulerDatabase(SQL sql) {
        try {
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
