package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.JobStatus;

@Setter
@Getter
public class JobQuery {

    JobStatus status;
    String type;
    int limit = -1;
    int offset = 0;

}
