package org.javawebstack.jobs.storage;

import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;

public class InMemoryJobStorageTest extends JobStorageTest {

    protected JobStorage initStorage() {
        return new InMemoryJobStorage();
    }

}
