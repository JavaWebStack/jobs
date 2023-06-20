package org.javawebstack.jobs.api.controller;

import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.router.annotation.PathPrefix;
import org.javawebstack.httpserver.router.annotation.With;
import org.javawebstack.httpserver.router.annotation.params.Body;
import org.javawebstack.httpserver.router.annotation.params.Path;
import org.javawebstack.httpserver.router.annotation.verbs.Delete;
import org.javawebstack.httpserver.router.annotation.verbs.Get;
import org.javawebstack.httpserver.router.annotation.verbs.Post;
import org.javawebstack.jobs.JobStatus;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.request.CreateJobRequest;
import org.javawebstack.jobs.api.response.JobResponse;
import org.javawebstack.jobs.api.response.Response;
import org.javawebstack.jobs.scheduler.model.JobScheduleEntry;
import org.javawebstack.jobs.storage.model.JobInfo;
import org.javawebstack.jobs.storage.model.JobQuery;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
        query.parsePaginationQuery(exchange);
        if(exchange.getQueryParameters().has("type"))
            query.setType(exchange.query("type"));
        if(exchange.getQueryParameters().has("status")) {
            query.setStatus(Stream.of(exchange.query("status").split(",")).map(JobStatus::valueOf).toArray(JobStatus[]::new));
        }

        List<JobInfo> jobList = storage.queryJobs(query);
        List<JobScheduleEntry> schedules = scheduler.getScheduleEntries(jobList.stream().map(JobInfo::getId).collect(Collectors.toList()));
        List<JobResponse> responses = jobList.stream()
                .map(j -> {
                    JobScheduleEntry schedule = schedules.stream().filter(s -> s.getJobId().equals(j.getId())).findFirst().orElse(null);
                    return new JobResponse(j, schedule);
                })
                .collect(Collectors.toList());

        return Response.success().setData(responses);
    }

    @Post
    public Response create(@Body CreateJobRequest request) {
        UUID id;
        if(request.getScheduleAt() != null) {
            id = jobs.schedule(request.getQueue(), request.getScheduleAt(), request.getType(), request.getPayload().toJsonString());
        } else {
            id = jobs.enqueue(request.getQueue(), request.getType(), request.getPayload().toJsonString());
        }
        JobInfo info = storage.getJob(id);
        if(info == null)
            return Response.error(500, "Failed to create the job");
        return Response.success().setData(info);
    }

    @Get("{uuid:id}")
    public Response get(@Path("id") UUID id, Exchange exchange) {
        JobInfo info = storage.getJob(id);
        if(info == null)
            return Response.error(404, "Job not found");
        AbstractObject res = exchange.getServer().getAbstractMapper().toAbstract(info).object();
        if(exchange.getQueryParameters().has("payload") && (exchange.query("payload").length() == 0 || exchange.query("payload").equals("true")))
            res.set("payload", AbstractElement.fromJson(storage.getJobPayload(info.getId())));
        return Response.success().setData(res);
    }

    @Delete("{uuid:id}")
    public Response delete(@Path("id") UUID id) {
        JobInfo info = storage.getJob(id);
        if (info == null)
            return Response.error(404, "Job not found");

        if (info.getStatus() == JobStatus.PROCESSING) {
            // TODO: Cancel job if running
            return Response.error(419, "Cannot abort jobs yet");
        } else {
            jobs.dequeue(id);
        }

        storage.deleteJob(id);

        return Response.success();
    }

    @Get("{uuid:jobid}/events")
    public Response getEvents(@Path("jobid") UUID jobId) {
        return Response.success().setData(storage.queryEvents(jobId));
    }

    @Get("{uuid:jobid}/events/{uuid:eventid}/logs")
    public Response getLogEntries(@Path("jobid") UUID jobId, @Path("eventid") UUID eventId) {
        return Response.success().setData(storage.queryLogEntries(eventId));
    }

}
