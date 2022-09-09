package org.javawebstack.jobs.handler.retry;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RetryContext {

    RetryReason reason;
    Throwable exception;

}
