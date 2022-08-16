package org.javawebstack.jobs.storage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.UUID;

public class RedisJobStorage implements JobStorage {

    private final JedisPool pool;

    public RedisJobStorage(String host, int port, String username, String password, int database) {
        JedisPoolConfig jedisConfig = new JedisPoolConfig();
        jedisConfig.setMaxTotal(5);
        this.pool = new JedisPool(jedisConfig, host, port, 2000, username, password, database);
    }

    public RedisJobStorage(JedisPool pool) {
        this.pool = pool;
    }

    public void createJob(String queue, long currentTime, JobData data) {
        Jedis jedis = pool.getResource();
        try {
            jedis.set("jobs:" + data.getId() + ":type", data.getType());
            jedis.set("jobs:" + data.getId() + ":available_at", String.valueOf(data.getAvailableAt()));
            jedis.set("jobs:" + data.getId() + ":data", data.getData());
            jedis.zadd("queue:" + queue, data.getAvailableAt(), data.getId().toString());
            pool.returnResource(jedis);
        } catch (Exception ex) {
            pool.returnResource(jedis);
            throw ex;
        }
    }

    public JobData pollJob(String queue, long currentTime) {
        Jedis jedis = pool.getResource();
        try {
            Object polled = jedis.eval("local a=redis.call('zpopmin',KEYS[1],1)if a==false or#a==0 then return end;if tonumber(a[2])>tonumber(ARGV[1])then redis.call('zadd',KEYS[1],a[2],a[1])return end;return a[1]", 1, "queue:" + queue, String.valueOf(currentTime));
            pool.returnResource(jedis);
            if(!(polled instanceof String))
                return null;
            UUID id = UUID.fromString((String) polled);
            return getJob(id);
        } catch (Exception ex) {
            pool.returnResource(jedis);
            throw ex;
        }
    }

    public JobData getJob(UUID id) {
        Jedis jedis = pool.getResource();
        try {
            String type = jedis.get("jobs:" + id + ":type");
            String data = jedis.get("jobs:" + id + ":data");
            long availableAt = Long.parseLong(jedis.get("jobs:" + id + ":available_at"));
            pool.returnResource(jedis);
            return new JobData()
                    .setId(id)
                    .setType(type)
                    .setData(data)
                    .setAvailableAt(availableAt);
        } catch (Exception ex) {
            pool.returnResource(jedis);
            throw ex;
        }
    }

}
