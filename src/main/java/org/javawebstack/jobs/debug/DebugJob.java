package org.javawebstack.jobs.debug;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.javawebstack.jobs.Job;
import org.javawebstack.jobs.JobContext;

@AllArgsConstructor
@NoArgsConstructor
public class DebugJob implements Job {

    String type;
    Integer time;
    String message;

    public void execute(JobContext context) throws Exception {
        if(type == null)
            type = "";
        if(time != null)
            Thread.sleep(time);
        switch (type) {
            case "fail": {
                context.fail(message);
            }
            case "exception": {
                throw new RuntimeException(message);
            }
            case "log": {
                context.info(message);
                return;
            }
        }
    }

}
