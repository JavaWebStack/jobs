package org.javawebstack.jobs.api.middleware;

import lombok.AllArgsConstructor;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.RequestHandler;
import org.javawebstack.jobs.api.JobApi;
import org.javawebstack.jobs.api.response.Response;

@AllArgsConstructor
public class AuthMiddleware implements RequestHandler {

    public JobApi api;

    public Object handle(Exchange exchange) {
        String bearer = exchange.bearerAuth();
        if(bearer != null && bearer.length() == 0)
            bearer = null;
        String username = api.getAuthProvider().checkToken(bearer);
        if(username != null) {
            exchange.attrib("username", username);
            return null;
        }
        return Response.error(401, "Authentication failed");
    }

}
