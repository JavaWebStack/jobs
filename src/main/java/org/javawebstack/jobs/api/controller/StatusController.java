package org.javawebstack.jobs.api.controller;

import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.jobs.Job;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.response.Response;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PathPrefix("api/status")
@With("jobs_auth")
public class StatusController extends Controller {

    private final List<Class<?>> availableJobClasses;

    public StatusController(Jobs jobs) {
        super(jobs);
        this.availableJobClasses = new ArrayList<>(new Reflections().getSubTypesOf(Job.class));
    }

    @Get("job-counts")
    public Response jobCounts() {
        return Response.success().setData(storage.getJobCountsByStatuses());
    }

    @Get("job-types")
    public Response jobTypes() {
        return Response.success().setData(availableJobClasses.stream().map(Class::getName).collect(Collectors.toList()));
    }

}
