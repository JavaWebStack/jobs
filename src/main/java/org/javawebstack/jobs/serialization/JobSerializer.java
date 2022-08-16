package org.javawebstack.jobs.serialization;

import org.javawebstack.jobs.Job;

public interface JobSerializer {

    String serialize(Job job);

    <T extends Job> T deserialize(Class<T> type, String s);

}
