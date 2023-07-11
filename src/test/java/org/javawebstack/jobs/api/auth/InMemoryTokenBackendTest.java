package org.javawebstack.jobs.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTokenBackendTest {
    private static final String USERNAME = "test";
    private InMemoryTokenBackend tokenBackend;

    @BeforeEach
    public void setUp() {
        tokenBackend = new InMemoryTokenBackend();
    }

    @Test
    public void testDifferentToken() {
        assertNotEquals(tokenBackend.createToken(USERNAME), tokenBackend.createToken("test"));
    }

    @Test
    public void testNullToken() {
        assertNull(tokenBackend.checkToken(null));
    }

    @Test
    public void testValidToken() {
        String token = tokenBackend.createToken(USERNAME);
        assertNotNull(token);
        assertEquals(USERNAME, tokenBackend.checkToken(token));
    }

    @Test
    public void testTokenDeletion() {
        String token = tokenBackend.createToken(USERNAME);
        assertNotNull(token);
        assertEquals(USERNAME, tokenBackend.checkToken(token));
        tokenBackend.deleteToken(token);
        assertNull(tokenBackend.checkToken(token));
    }
}