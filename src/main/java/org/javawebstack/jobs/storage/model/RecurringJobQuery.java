package org.javawebstack.jobs.storage.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecurringJobQuery extends PaginationQuery<RecurringJobQuery> {

    String type;

}
