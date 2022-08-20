package org.javawebstack.jobs.storage.sql.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.storage.model.JobEventData;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Dates
public class JobEventModel extends Model {

    @Column
    UUID id;
    @Column
    UUID jobId;
    @Column
    JobEventData.Type type;
    @Column
    Timestamp createdAt;
    @Column
    Timestamp updatedAt;

}
