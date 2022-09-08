package org.javawebstack.jobs.api.request;

import lombok.Getter;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.validator.Rule;

import java.util.Date;

@Getter
public class CreateJobRequest {

    @Rule("required")
    String queue;
    @Rule({})
    Date scheduleAt;
    @Rule("required")
    String type;
    @Rule({})
    AbstractObject payload;

}
