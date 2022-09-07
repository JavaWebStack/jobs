package org.javawebstack.jobs.standalone;

import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.JobApi;

public class StandaloneMain {

    public static void main(String[] args) {
        StandaloneOptions options = new StandaloneOptions();
        Jobs jobs = new Jobs(
                options.getStorage(),
                options.getScheduler(),
                options.getSerializer()
        );
        JobApi api = new JobApi(jobs);
        api.auth(options.getAuthProvider());
        api.dashboard(options.isEnabled("dashboard", false));
        api.start(options.getInt("api.port", 8080));
    }

}
