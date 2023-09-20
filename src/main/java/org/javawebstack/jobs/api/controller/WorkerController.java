package org.javawebstack.jobs.api.controller;

import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.response.Response;

@PathPrefix("api/workers")
@With("jobs_auth")
public class WorkerController extends Controller {

    public WorkerController(Jobs jobs) {
        super(jobs);
    }

    @Get
    public Response list() {
        return Response.success().setData(storage.queryWorkers());
    }

}
