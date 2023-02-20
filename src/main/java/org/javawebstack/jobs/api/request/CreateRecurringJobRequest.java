package org.javawebstack.jobs.api.request;

import lombok.Getter;
import org.javawebstack.validator.Rule;

@Getter
public class CreateRecurringJobRequest {

    @Rule("required")
    String queue;
    @Rule("required")
    String type;
    @Rule("required")
    String cron;

}
