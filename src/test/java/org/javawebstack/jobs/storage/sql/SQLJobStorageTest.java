package org.javawebstack.jobs.storage.sql;

import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.JobStorageTest;
import org.javawebstack.jobs.storage.sql.SQLJobStorage;
import org.javawebstack.jobs.test.TestProperties;
import org.javawebstack.jobs.test.TestUtil;
import org.javawebstack.jobs.test.precondition.SQLDatabaseAvailable;
import org.javawebstack.orm.wrapper.SQL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SQLDatabaseAvailable.class)
public class SQLJobStorageTest extends JobStorageTest {

    private SQL sql;

    protected JobStorage initStorage() {
        sql = TestProperties.createSQLDatabaseConnection();
        TestUtil.purgeStorageDatabase(sql);
        return new SQLJobStorage(sql, null);
    }

    @AfterAll
    public void cleanup() {
        TestUtil.purgeStorageDatabase(sql);
    }

}
