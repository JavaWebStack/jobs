package org.javawebstack.jobs.api.controller;

import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.response.Response;

@PathPrefix("api/workers")
public class WorkerController extends Controller {

    public WorkerController(Jobs jobs) {
        super(jobs);
    }

    @Get
    public Response list() {
        return Response.success().setData(jobs.getStorage().queryWorkers());
    }

}
