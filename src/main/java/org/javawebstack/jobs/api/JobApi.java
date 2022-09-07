package org.javawebstack.jobs.api;

import lombok.Getter;
import org.javawebstack.httpserver.HTTPServer;
import org.javawebstack.httpserver.transformer.response.JsonResponseTransformer;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.auth.AuthProvider;
import org.javawebstack.jobs.api.controller.JobController;
import org.javawebstack.jobs.api.controller.WorkerController;
import org.javawebstack.jobs.api.middleware.AuthMiddleware;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        server.beforeInterceptor(ex -> {
            ex.header("Access-Control-Allow-Origin", "*");
            ex.header("Access-Control-Allow-Methods", "*");
            return false;
        });
        return server;
    }

    public JobApi install(HTTPServer server, String prefix) {
        if(prefix == null)
            prefix = "";
        if(prefix.length() > 0 && enableDashboard)
            throw new IllegalArgumentException("Prefix can not be set when the dashboard is enabled!");
        server
                .controller(prefix, new JobController(jobs))
                .controller(prefix, new WorkerController(jobs))
                .middleware("jobs_auth", new AuthMiddleware(this));
        if(enableDashboard) {
            server.get("/", ex -> {
                ex.redirect("/overview");
                return "";
            });
            server.staticResourceDirectory(prefix, JobApi.class.getClassLoader(), "dashboard");
            String html = loadHtml();
            server.get(prefix + "{*:path}", ex -> html);
        }
        // TODO install dashboard
        return this;
    }

    private String loadHtml() {
        try {
            InputStream is = JobApi.class.getClassLoader().getResourceAsStream("dashboard/index.html");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int r;
            while ((r = is.read(buffer)) != -1)
                baos.write(buffer, 0, r);
            is.close();
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }catch (IOException ex) {
            return null;
        }
    }

}
