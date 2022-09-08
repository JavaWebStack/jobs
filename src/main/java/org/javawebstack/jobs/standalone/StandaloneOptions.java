package org.javawebstack.jobs.standalone;

import org.javawebstack.jobs.api.auth.AuthProvider;
import org.javawebstack.jobs.scheduler.JobScheduler;
import org.javawebstack.jobs.scheduler.inmemory.InMemoryJobScheduler;
import org.javawebstack.jobs.scheduler.sql.SQLJobScheduler;
import org.javawebstack.jobs.serialization.JobSerializer;
import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.storage.JobStorage;
import org.javawebstack.jobs.storage.inmemory.InMemoryJobStorage;
import org.javawebstack.jobs.storage.sql.SQLJobStorage;
import org.javawebstack.orm.wrapper.MySQL;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StandaloneOptions {

    private static final Map<String, String> ENV_MAPPING = new HashMap<String, String>() {{
        put("AUTH_TYPE", "auth.type");
        put("AUTH_USERNAME", "auth.username");
        put("AUTH_PASSWORD", "auth.password");
        put("AUTH_TOKEN", "auth.token");
        put("STORAGE_TYPE", "storage.type");
        put("SCHEDULER_TYPE", "scheduler.type");
        put("SERIALIZER", "serializer");
        put("DB_HOST", "db.host");
        put("DB_PORT", "db.port");
        put("DB_NAME", "db.name");
        put("DB_USERNAME", "db.username");
        put("DB_PASSWORD", "db.password");
        put("DB_PREFIX", "db.prefix");
        put("API_PORT", "api.port");
        put("DASHBOARD", "dashboard");
        put("WORKER_QUEUES", "worker.queues");
        put("WORKER_THREADS", "worker.threads");
        put("WORKER_INTERVAL", "worker.interval");
    }};

    final Map<String, String> options = new HashMap<>();

    public StandaloneOptions() {
        ENV_MAPPING.forEach((k, option) -> {
            String e = System.getenv(k);
            if(e != null)
                options.put(option, e);
        });
    }

    public boolean has(String key) {
        return options.containsKey(key);
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String orElse) {
        return options.getOrDefault(key, orElse);
    }

    public int getInt(String key, int orElse) {
        String v = get(key);
        if(v == null || v.length() == 0)
            return orElse;
        return Integer.parseInt(v);
    }

    public boolean isEnabled(String key, boolean orElse) {
        String v = get(key);
        if(v == null || v.length() == 0)
            return orElse;
        switch (v.toLowerCase(Locale.ROOT)) {
            case "1":
            case "true":
            case "yes":
            case "on":
                return true;
            default:
                return false;
        }
    }

    private MySQL getMySQL() {
        return new MySQL(
                get("db.host", "127.0.0.1"),
                getInt("db.port", 3306),
                get("db.name", "jobs"),
                get("db.username", "jobs"),
                get("db.password", "")
        );
    }

    public JobSerializer getSerializer() {
        switch (get("serializer", "json")) {
            default: {
                return new JsonJobSerializer();
            }
        }
    }

    public AuthProvider getAuthProvider() {
        switch (get("auth.type", "none")) {
            case "token": {
                return AuthProvider.token(get("auth.token"));
            }
            case "login": {
                return AuthProvider.usernameAndPassword(
                        get("auth.username"),
                        get("auth.password")
                );
            }
            default: {
                return AuthProvider.noAuth();
            }
        }
    }

    public JobStorage getStorage() {
        switch (get("storage.type", "in-memory")) {
            case "mysql": {
                return new SQLJobStorage(
                        getMySQL(),
                        get("db.prefix", "")
                );
            }
            default: {
                return new InMemoryJobStorage();
            }
        }
    }

    public JobScheduler getScheduler() {
        switch (get("scheduler.type", "in-memory")) {
            case "mysql": {
                return new SQLJobScheduler(
                        getMySQL(),
                        get("db.prefix", "")
                );
            }
            default: {
                return new InMemoryJobScheduler();
            }
        }
    }

}
