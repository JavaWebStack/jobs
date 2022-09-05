package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.JobStatus;

@Setter
@Getter
public class JobQuery {

    JobStatus[] status;
    String type;
    int limit = -1;
    int offset = 0;

    public JobQuery setStatus(JobStatus... status) {
        this.status = status.length == 0 ? null : status;
        return this;
    }

}
