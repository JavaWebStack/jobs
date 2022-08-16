package org.javawebstack.jobs.storage;

import org.javawebstack.orm.wrapper.MySQL;
import org.javawebstack.orm.wrapper.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLJobStorage implements JobStorage {

    private final SQL sql;
    private final String tablePrefix;

    public MySQLJobStorage(String host, int port, String database, String username, String password, String tablePrefix) {
        this(new MySQL(host, port, database, username, password), tablePrefix);
    }

    public MySQLJobStorage(SQL sql, String tablePrefix) {
        this.sql = sql;
        this.tablePrefix = tablePrefix;
        try {
            sql.write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "jobs` (`id` VARCHAR(36) NOT NULL, `queue` VARCHAR(50) NOT NULL, `type` VARCHAR(100) NOT NULL, `data` LONGTEXT NOT NULL, `available_at` BIGINT NOT NULL, `created_at` BIGINT NOT NULL, PRIMARY KEY(`id`));");
            sql.write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "queue` (`id` INT NOT NULL AUTO_INCREMENT, `queue` VARCHAR(50) NOT NULL, `job_id` VARCHAR(36) NOT NULL, `available_at` BIGINT NOT NULL, PRIMARY KEY(`id`));");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createJob(String queue, long currentTime, JobData data) {
        try {
            sql.write("INSERT INTO `" + tablePrefix + "jobs` (`id`,`queue`,`type`,`data`,`available_at`,`created_at`) VALUES (?,?,?,?,?,?);", data.getId().toString(), queue, data.getType(), data.getData(), data.getAvailableAt(), currentTime);
            sql.write("INSERT INTO `" + tablePrefix + "queue` (`queue`,`job_id`,`available_at`) VALUES (?,?,?);", queue, data.getId().toString(), data.getAvailableAt());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized JobData pollJob(String queue, long currentTime) {
        try {
            ResultSet rs = sql.read("SELECT `id`,`job_id` FROM `" + tablePrefix + "queue` WHERE `queue`=? AND `available_at`<=? ORDER BY `id` ASC LIMIT 1;");
            if(rs.next()) {
                UUID id = UUID.fromString(rs.getString("job_id"));
                sql.close(rs);
                sql.write("DELETE FROM `" + tablePrefix + "queue` WHERE `id`=?;", id.toString());
                return getJob(id);
            }
            sql.close(rs);
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public JobData getJob(UUID id) {
        try {
            ResultSet rs = sql.read("SELECT * FROM `" + tablePrefix + "jobs` WHERE `id`=?;", id.toString());
            if(rs.next()) {
                JobData data = new JobData()
                        .setId(id)
                        .setType(rs.getString("type"))
                        .setData(rs.getString("data"))
                        .setAvailableAt(rs.getLong("available_at"));
                sql.close(rs);
                return data;
            }
            sql.close(rs);
            return null;
        } catch (SQLException ex) {
            return null;
        }
    }

}
