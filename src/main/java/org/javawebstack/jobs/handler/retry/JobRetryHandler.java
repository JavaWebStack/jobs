package org.javawebstack.jobs.handler.retry;

import org.javawebstack.jobs.JobContext;

public interface JobRetryHandler {

    void handleRetry(JobContext jobContext, RetryContext retryContext);

}
