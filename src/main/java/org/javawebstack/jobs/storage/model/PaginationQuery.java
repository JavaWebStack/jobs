package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.httpserver.Exchange;

@Getter
@Setter
public abstract class PaginationQuery {

    int limit = -1;
    int offset = 0;

    public void parsePaginationQuery(Exchange exchange) {
        if(exchange.getQueryParameters().has("page")) {
            int page = Integer.parseInt(exchange.query("page"));
            int pageSize = 10;
            if(exchange.getQueryParameters().has("page_size"))
                pageSize = Integer.parseInt(exchange.query("page_size"));
            offset = (page - 1) * pageSize;
            limit = pageSize;
        }
    }

}
