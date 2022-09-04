package org.javawebstack.jobs.util;

public class SyncTimer {

    Runnable runnable;
    long next;
    long interval;

    public SyncTimer(Runnable runnable, long first, long interval) {
        this.runnable = runnable;
        this.next = first;
        this.interval = interval;
    }

    public boolean tick() {
        long now = System.currentTimeMillis();
        if(now >= next) {
            next = now + interval;
            runnable.run();
            return true;
        }
        return false;
    }

}
