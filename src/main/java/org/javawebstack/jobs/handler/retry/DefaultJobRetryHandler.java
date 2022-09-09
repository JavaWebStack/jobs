package org.javawebstack.jobs.handler.retry;

import org.javawebstack.jobs.JobContext;

import java.time.Instant;
import java.util.Date;

public class DefaultJobRetryHandler implements JobRetryHandler {

    public void handleRetry(JobContext jobContext, RetryContext retryContext) {
        Date at = Date.from(Instant.now().plusSeconds(interval(jobContext.getCurrentRetry())));
        if(retryContext.getReason() == RetryReason.REQUESTED) {
            jobContext.schedule(jobContext.getQueue(), at);
        } else {
            if(jobContext.getCurrentRetry() + 1 < jobContext.getMaxRetries()) {
                jobContext.schedule(jobContext.getQueue(), at);
            }
        }
    }

    protected int interval(int retry) {
        switch (retry) {
            case 0:
                return 10; // 10 seconds
            case 1:
                return 60; // 1 minute
            case 2:
                return 300; // 5 minutes
            case 3:
                return 600; // 10 minutes
            case 4:
                return 1800; // 30 minutes
            case 5:
                return 3600; // 1 hour
            case 6:
                return 7200; // 2 hours
            case 7:
                return 21600; // 6 hours
            case 8:
                return 43200; // 12 hours
            default:
                return 86400; // 1 day
        }
    }

}
