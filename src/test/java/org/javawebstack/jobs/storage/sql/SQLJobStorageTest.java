package org.javawebstack.jobs.storage.sql;

import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.JobStorageTest;
import org.javawebstack.jobs.test.TestProperties;
import org.javawebstack.jobs.test.TestUtil;
import org.javawebstack.jobs.test.precondition.SQLDatabaseAvailable;
import org.javawebstack.orm.connection.pool.SQLPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SQLDatabaseAvailable.class)
public class SQLJobStorageTest extends JobStorageTest {

    private SQLPool pool;

    protected JobStorage initStorage() {
        pool = TestProperties.createSQLDatabaseConnection();
        TestUtil.purgeStorageDatabase(pool);
        return new SQLJobStorage(pool, null);
    }

    @AfterEach
    public void cleanup() {
        TestUtil.purgeStorageDatabase(pool);
    }

}
