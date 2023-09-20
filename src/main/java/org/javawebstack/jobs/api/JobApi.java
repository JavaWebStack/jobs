package org.javawebstack.jobs.api;

import lombok.Getter;
import org.javawebstack.http.router.HTTPMethod;
import org.javawebstack.http.router.HTTPRouter;
import org.javawebstack.http.router.transformer.response.JsonResponseTransformer;
import org.javawebstack.http.router.undertow.UndertowHTTPSocketServer;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.auth.AuthProvider;
import org.javawebstack.jobs.api.controller.*;
import org.javawebstack.jobs.api.middleware.AuthMiddleware;
import org.javawebstack.jobs.api.middleware.ResponseMiddleware;

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

    public HTTPRouter start(int port) {
        HTTPRouter router = new HTTPRouter(new UndertowHTTPSocketServer())
                .responseTransformer(new JsonResponseTransformer().ignoreStrings())
                .port(port);
        install(router, null);
        router.start();
        router.beforeInterceptor(ex -> {
            ex.header("Access-Control-Allow-Origin", "*");
            ex.header("Access-Control-Allow-Methods", "*");
            ex.header("Access-Control-Allow-Headers", "*");
            if(ex.getMethod() == HTTPMethod.OPTIONS) {
                ex.close();
                return true;
            }
            return false;
        });
        return router;
    }

    public JobApi install(HTTPRouter router, String prefix) {
        if(prefix == null)
            prefix = "";
        if(prefix.length() > 0 && enableDashboard)
            throw new IllegalArgumentException("Prefix can not be set when the dashboard is enabled!");
        router
                .exceptionHandler(new ErrorController())
                .controller(prefix, new JobController(jobs))
                .controller(prefix, new RecurringJobController(jobs))
                .controller(prefix, new StatusController(jobs))
                .controller(prefix, new WorkerController(jobs))
                .afterAny(prefix + "{*:path}", new ResponseMiddleware())
                .middleware("jobs_auth", new AuthMiddleware(this));
        if(enableDashboard) {
            router.get("/", ex -> {
                ex.redirect("/overview");
                return "";
            });
            router.staticResourceDirectory(prefix, JobApi.class.getClassLoader(), "dashboard");
            String html = loadHtml();
            router.get(prefix + "{*:path}", ex -> html);
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
