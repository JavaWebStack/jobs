package org.javawebstack.jobs.time;

public interface TimeProvider {

    long time();
    String format(long time);

}
