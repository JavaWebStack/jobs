package org.javawebstack.jobs.storage.sql;

import org.javawebstack.jobs.storage.model.JobData;
import org.javawebstack.jobs.storage.model.JobEventData;
import org.javawebstack.jobs.storage.model.JobLogData;
import org.javawebstack.jobs.storage.model.JobQuery;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.sql.model.JobEventModel;
import org.javawebstack.jobs.storage.sql.model.JobLogModel;
import org.javawebstack.jobs.storage.sql.model.JobModel;
import org.javawebstack.orm.ORM;
import org.javawebstack.orm.ORMConfig;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.exception.ORMConfigurationException;
import org.javawebstack.orm.query.Query;
import org.javawebstack.orm.wrapper.MySQL;
import org.javawebstack.orm.wrapper.SQL;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SQLJobStorage implements JobStorage {

    private final SQL sql;
    private final String tablePrefix;

    public SQLJobStorage(String host, int port, String database, String username, String password, String tablePrefix) {
        this(new MySQL(host, port, database, username, password), tablePrefix);
    }

    public SQLJobStorage(SQL sql, String tablePrefix) {
        this.sql = sql;
        this.tablePrefix = tablePrefix;
        try {
            ORMConfig config = new ORMConfig().setDefaultSize(255).setTablePrefix(tablePrefix != null ? tablePrefix : "");
            ORM.register(JobModel.class, sql, config);
            ORM.register(JobLogModel.class, sql, config);
            Repo.get(JobModel.class).autoMigrate();
            Repo.get(JobLogModel.class).autoMigrate();
        } catch (ORMConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void createJob(JobData data) {
        new JobModel()
                .setId(data.getId())
                .setType(data.getType())
                .setData(data.getData())
                .save();
    }

    public void deleteJob(UUID id) {
        Repo.get(JobModel.class).where("id", id).delete();
    }

    public List<JobData> queryJobs(JobQuery query) {
        Query<JobModel> q = Repo.get(JobModel.class).query();
        // Apply query
        return q.stream().map(JobModel::toJobData).collect(Collectors.toList());
    }

    public void createEvent(JobEventData data) {
        new JobEventModel()
                .setId(data.getId())
                .setJobId(data.getJobId())
                .setType(data.getType())
                .save();
    }

    public void createLog(JobLogData data) {
        new JobLogModel()
                .setId(data.getId())
                .setEventId(data.getEventId())
                .setLevel(data.getLevel())
                .setMessage(data.getMessage())
                .save();
    }

    public JobData getJob(UUID id) {
        JobModel m = Repo.get(JobModel.class).where("id", id).first();
        if(m == null)
            return null;
        return m.toJobData();
    }

}
