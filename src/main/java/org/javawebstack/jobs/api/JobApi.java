package org.javawebstack.jobs.api;

import lombok.Getter;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.httpserver.transformer.response.JsonResponseTransformer;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.auth.AuthProvider;
import org.javawebstack.jobs.api.controller.JobController;
import org.javawebstack.jobs.api.middleware.AuthMiddleware;

public class JobApi {

    @Getter
    final Jobs jobs;
    @Getter
    AuthProvider authProvider = AuthProvider.noAuth();
    boolean enableDashboard = false;

    public JobApi(Jobs jobs) {
        this.jobs = jobs;
    }

    public JobApi auth(AuthProvider provider) {
        this.authProvider = provider;
        return this;
    }

    public JobApi enableDashboard() {
        return dashboard(true);
    }

    public JobApi dashboard(boolean enableDashboard) {
        this.enableDashboard = enableDashboard;
        return this;
    }

    public HTTPServer start(int port) {
        HTTPServer server = new HTTPServer()
                .responseTransformer(new JsonResponseTransformer().ignoreStrings())
                .port(port);
        install(server, null);
        server.start();
        return server;
    }

    public JobApi install(HTTPServer server, String prefix) {
        if(prefix == null)
            prefix = "";
        server
                .controller(prefix, new JobController(jobs))
                .middleware("jobs_auth", new AuthMiddleware(this));
        // TODO install dashboard
        return this;
    }

}
