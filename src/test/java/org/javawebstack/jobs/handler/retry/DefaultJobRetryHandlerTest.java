package org.javawebstack.jobs.handler.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultJobRetryHandlerTest {
    @Test
    public void testRetryInterval() {
        DefaultJobRetryHandler retryHandler = new DefaultJobRetryHandler();
        int[] testIntervals = new int[] { 10, 60, 300, 600, 1800, 3600, 7200, 21600, 43200, 86400};
        for (int i = 0; i < testIntervals.length; i++)
            assertEquals(testIntervals[i], retryHandler.interval(i));
    }
}