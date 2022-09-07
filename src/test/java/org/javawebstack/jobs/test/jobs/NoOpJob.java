package org.javawebstack.jobs.test.jobs;

import org.javawebstack.jobs.Job;
import org.javawebstack.jobs.JobContext;

import java.util.*;

public class NoOpJob implements Job {

    private static final Map<UUID, Integer> executions = new HashMap<>();

    public static int getExecutions(UUID id) {
        return executions.getOrDefault(id, 0);
    }

    public void execute(JobContext context) throws Exception {
        executions.put(context.getId(), executions.getOrDefault(context.getId(), 0) + 1);
    }

}
