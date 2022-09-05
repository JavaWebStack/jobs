package org.javawebstack.jobs.api.controller;

import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.response.Response;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobQuery;

import java.util.UUID;
import java.util.stream.Stream;

@PathPrefix("api/jobs")
@With("jobs_auth")
public class JobController extends Controller {

    public JobController(Jobs jobs) {
        super(jobs);
    }

    @Get
    public Response list(Exchange exchange) {
        JobQuery query = new JobQuery();
        if(exchange.getQueryParameters().has("type"))
            query.setType(exchange.query("type"));
        if(exchange.getQueryParameters().has("status")) {
            query.setStatus(Stream.of(exchange.query("status").split(",")).map(JobStatus::valueOf).toArray(JobStatus[]::new));
        }
        return Response.success().setData(storage.queryJobs(query));
    }

    @Get("{uuid:id}")
    public Response get(@Path("id") UUID id) {
        JobInfo info = storage.getJob(id);
        if(info == null)
            return Response.error(404, "Job not found");
        return Response.success().setData(info);
    }

}
