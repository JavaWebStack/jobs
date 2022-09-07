package org.javawebstack.jobs.scheduler;

import org.javawebstack.jobs.scheduler.sql.SQLJobScheduler;
import org.javawebstack.jobs.test.TestProperties;
import org.javawebstack.jobs.test.TestUtil;
import org.javawebstack.jobs.test.precondition.SQLDatabaseAvailable;
import org.javawebstack.orm.wrapper.SQL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SQLDatabaseAvailable.class)
public class SQLJobSchedulerTest extends JobSchedulerTest {

    private SQL sql;

    protected JobScheduler initScheduler() {
        sql = TestProperties.createSQLDatabaseConnection();
        TestUtil.purgeSchedulerDatabase(sql);
        return new SQLJobScheduler(sql, null);
    }

    @AfterAll
    public void cleanup() {
        TestUtil.purgeSchedulerDatabase(sql);
    }

}
