package org.javawebstack.jobs.storage.sql.model;

import lombok.Getter;
import lombok.Setter;
import org.javawebstack.jobs.storage.model.JobData;
import org.javawebstack.orm.Model;
import org.javawebstack.orm.annotation.Column;
import org.javawebstack.orm.annotation.Dates;
import org.javawebstack.orm.annotation.SoftDelete;
import org.javawebstack.orm.annotation.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@Dates
@SoftDelete
@Table("jobs")
public class JobModel extends Model {

    @Column
    UUID id;
    @Column
    String type;
    @Column
    String data;
    @Column
    Timestamp createdAt;
    @Column
    Timestamp updatedAt;
    @Column
    Timestamp deletedAt;

    public JobData toJobData() {
        return new JobData()
                .setId(id)
                .setType(type)
                .setData(data);
    }

}
