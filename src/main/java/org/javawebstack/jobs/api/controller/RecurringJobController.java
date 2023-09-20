package org.javawebstack.jobs.api.controller;

import org.javawebstack.abstractdata.AbstractObject;

import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.PathPrefix;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Body;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Delete;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.router.annotation.verbs.Post;
import org.javawebstack.jobs.Jobs;
import org.javawebstack.jobs.api.request.CreateRecurringJobRequest;
import org.javawebstack.jobs.api.response.Response;
import org.javawebstack.jobs.storage.model.RecurringJobInfo;
import org.javawebstack.jobs.storage.model.RecurringJobQuery;

import java.util.UUID;


@PathPrefix("api/recurring-jobs")
@With("jobs_auth")
public class RecurringJobController extends Controller {

    public RecurringJobController(Jobs jobs) {
        super(jobs);
    }

    @Get
    public Response list(Exchange exchange) {
        RecurringJobQuery query = new RecurringJobQuery();
        query.parsePaginationQuery(exchange);
        if(exchange.getQueryParameters().has("type"))
            query.setType(exchange.query("type"));
        return Response.success().setData(storage.queryRecurringJobs(query));
    }

    @Post
    public Response create(@Body CreateRecurringJobRequest request) {
        UUID id = jobs.scheduleRecurrently(request.getQueue(), request.getCron(), request.getType(), request.getPayload().toJsonString());
        RecurringJobInfo info = storage.getRecurringJob(id);
        if(info == null)
            return Response.error(500, "Failed to create the recurring job");
        return Response.success().setData(info);
    }

    @Get("{uuid:id}")
    public Response get(@Path("id") UUID id, Exchange exchange) {
        RecurringJobInfo info = storage.getRecurringJob(id);
        if(info == null)
            return Response.error(404, "Recurring job not found");
        AbstractObject res = exchange.getRouter().getMapper().map(info).object();
        return Response.success().setData(res);
    }

    @Delete("{uuid:id}")
    public Response delete(@Path("id") UUID id) {
        RecurringJobInfo info = storage.getRecurringJob(id);
        if (info == null)
            return Response.error(404, "Recurring job not found");

        storage.deleteRecurringJob(id);

        return Response.success();
    }

}
