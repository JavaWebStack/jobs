package org.javawebstack.jobs.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCredentialBackendTest {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private InMemoryCredentialBackend credentials;

    @BeforeEach
    public void setUp() {
        credentials = new InMemoryCredentialBackend();
        credentials.addUser(USERNAME, PASSWORD);
    }

    @Test
    public void checkCreatedUser() {
        assertEquals(1, credentials.getUsers().size());
    }

    @Test
    public void testCredentials() {
        assertTrue(credentials.checkCredentials(USERNAME, PASSWORD));
        assertFalse(credentials.checkCredentials(USERNAME, "wrong"));
    }

    @Test
    public void testUnknownUser() {
        assertFalse(credentials.checkCredentials("test", "test"));
    }
}