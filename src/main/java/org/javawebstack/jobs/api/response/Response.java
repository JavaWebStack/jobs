package org.javawebstack.jobs.api.response;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Response {

    transient int status;
    boolean success;
    Object data;
    Pagination pagination;
    List<String> errors;

    private Response(int status) {
        this.status = status;
        this.success = status < 300;
    }

    @AllArgsConstructor
    public static class Pagination {
        int total;
    }

    public Response addError(String error) {
        if(this.errors == null)
            this.errors = new ArrayList<>();
        this.errors.add(error);
        return this;
    }

    public Response setData(Object data) {
        this.data = data;
        return this;
    }

    public Response setPagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    public static Response success() {
        return success(200);
    }

    public static Response success(int status) {
        return new Response(status);
    }

    public static Response error(String message) {
        return error(500, message);
    }

    public static Response error(int status, String message) {
        return new Response(status).addError(message);
    }

}
