package org.javawebstack.jobs.handler;

import org.javawebstack.jobs.JobContext;

public interface JobExceptionHandler {

    void handleException(JobContext context, Throwable t);

}
