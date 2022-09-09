package org.javawebstack.jobs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SyncTimerTest {

    @Test
    public void testTiming() throws InterruptedException {
        SyncTimer timerInFuture = new SyncTimer(() -> {
            throw new RuntimeException("executed");
        }, System.currentTimeMillis() + 10000, 1000);
        assertDoesNotThrow(timerInFuture::tick);
        SyncTimer timerInPast = new SyncTimer(() -> {
            throw new RuntimeException("executed");
        }, 0, 50);
        assertThrows(RuntimeException.class, timerInPast::tick);
        assertDoesNotThrow(timerInPast::tick);
        Thread.sleep(60);
        assertThrows(RuntimeException.class, timerInPast::tick);
    }

}
