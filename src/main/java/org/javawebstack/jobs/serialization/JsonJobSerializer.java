package org.javawebstack.jobs.serialization;

import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.mapper.Mapper;
import org.javawebstack.abstractdata.mapper.naming.NamingPolicy;
import org.javawebstack.jobs.Job;

public class JsonJobSerializer implements JobSerializer {

    private final Mapper mapper;

    public JsonJobSerializer() {
        this(new Mapper().namingPolicy(NamingPolicy.SNAKE_CASE));
    }

    public JsonJobSerializer(Mapper mapper) {
        this.mapper = mapper;
    }

    public String serialize(Job job) {
        try {
            Class.forName(job.getClass().getName());
            return mapper.map(job).object().toJsonString(true);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Job must be a proper class and cannot be a dynamic class or lambda!");
        }
    }

    public <T extends Job> T deserialize(Class<T> type, String s) {
        return mapper.map(AbstractElement.fromJson(s).object(), type);
    }

}
