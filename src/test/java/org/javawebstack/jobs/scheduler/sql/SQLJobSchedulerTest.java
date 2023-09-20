package org.javawebstack.jobs.scheduler.sql;

import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.JobSchedulerTest;
import org.javawebstack.jobs.test.TestProperties;
import org.javawebstack.jobs.test.TestUtil;
import org.javawebstack.jobs.test.precondition.SQLDatabaseAvailable;
import org.javawebstack.orm.connection.pool.SQLPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SQLDatabaseAvailable.class)
public class SQLJobSchedulerTest extends JobSchedulerTest {

    private SQLPool pool;

    protected JobScheduler initScheduler() {
        pool = TestProperties.createSQLDatabaseConnection();
        TestUtil.purgeSchedulerDatabase(pool);
        return new SQLJobScheduler(pool, null);
    }

    @AfterAll
    public void cleanup() {
        TestUtil.purgeSchedulerDatabase(pool);
    }

}
