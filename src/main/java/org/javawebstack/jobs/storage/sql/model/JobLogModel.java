package org.javawebstack.jobs.storage.sql.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Dates
@Table("job_logs")
public class JobLogModel extends Model {

    @Column
    UUID id;
    @Column
    UUID eventId;
    @Column
    String level;
    @Column(size = Integer.MAX_VALUE)
    String message;
    @Column
    Timestamp createdAt;
    @Column
    Timestamp updatedAt;

}
