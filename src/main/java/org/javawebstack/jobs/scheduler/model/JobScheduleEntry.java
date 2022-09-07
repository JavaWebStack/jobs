package org.javawebstack.jobs.scheduler.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class JobScheduleEntry {

    UUID jobId;
    Date at;

}
