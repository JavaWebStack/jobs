package org.javawebstack.jobs.storage.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.JobStatus;

@Setter
@Getter
public class JobQuery extends PaginationQuery<JobQuery> {

    JobStatus[] status;
    String type;

    public JobQuery setStatus(JobStatus... status) {
        this.status = status.length == 0 ? null : status;
        return this;
    }

}
