package org.javawebstack.jobs.storage.model;

import org.javawebstack.httpserver.Exchange;

public abstract class PaginationQuery<T extends PaginationQuery<T>> {

    protected int limit = -1;
    protected int offset = 0;

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

    public int getLimit() {
        return limit;
    }

    public T setLimit(int limit) {
        this.limit = limit;
        return (T) this;
    }

    public int getOffset() {
        return offset;
    }

    public T setOffset(int offset) {
        this.offset = offset;
        return (T) this;
    }
}
