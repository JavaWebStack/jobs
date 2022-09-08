package org.javawebstack.jobs.api.middleware;

import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.AfterRequestHandler;
import org.javawebstack.jobs.api.response.Response;

public class ResponseMiddleware implements AfterRequestHandler {

    public Object handleAfter(Exchange exchange, Object o) {
        if(o instanceof Response) {
            Response response = (Response) o;
            exchange.status(response.getStatus());
        }
        return o;
    }

}
