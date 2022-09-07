package org.javawebstack.jobs.test;

import org.javawebstack.orm.wrapper.MySQL;
import org.javawebstack.orm.wrapper.SQL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class TestProperties {

    private static final Properties properties = new Properties();

    static {
        String testPropertiesPath = System.getenv("TEST_PROPERTIES");
        if(testPropertiesPath == null || testPropertiesPath.length() == 0)
            testPropertiesPath = "test.properties";
        File testPropertiesFile = new File(testPropertiesPath);
        if(testPropertiesFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(testPropertiesFile);
                properties.load(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isSQLDatabaseAvailable() {
        return properties.containsKey("database.mysql.host");
    }

    public static SQL createSQLDatabaseConnection() {
        return new MySQL(
                properties.getProperty("database.mysql.host"),
                Integer.parseInt(properties.getProperty("database.mysql.port", "3306")),
                properties.getProperty("database.mysql.name", "jobs"),
                properties.getProperty("database.mysql.username", "jobs"),
                properties.getProperty("database.mysql.password", "")
        );
    }

}
