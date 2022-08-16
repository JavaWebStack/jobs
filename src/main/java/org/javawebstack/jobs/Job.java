package org.javawebstack.jobs;

public interface Job {

    void execute(JobContext context) throws Exception;

}
