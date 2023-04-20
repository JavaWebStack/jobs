package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RecurringJobQuery extends PaginationQuery<RecurringJobQuery> {

    String queue;
    String type;
    Date sinceLastExecution;

}
