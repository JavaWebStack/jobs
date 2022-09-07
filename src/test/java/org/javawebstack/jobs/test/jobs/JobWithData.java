package org.javawebstack.jobs.test.jobs;

import lombok.*;
import org.javawebstack.jobs.Job;
import org.javawebstack.jobs.JobContext;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class JobWithData implements Job {

    String data;

    public void execute(JobContext context) throws Exception {

    }

}
