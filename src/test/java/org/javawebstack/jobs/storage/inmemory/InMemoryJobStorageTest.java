package org.javawebstack.jobs.storage.inmemory;

import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.JobStorageTest;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;

public class InMemoryJobStorageTest extends JobStorageTest {

    protected JobStorage initStorage() {
        return new InMemoryJobStorage();
    }

}
