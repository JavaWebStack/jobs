package org.javawebstack.jobs.serialization;

import org.javawebstack.jobs.serialization.JsonJobSerializer;
import org.javawebstack.jobs.test.jobs.JobWithData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonJobSerializerTest {

    JsonJobSerializer serializer;

    @BeforeAll
    public void up() {
        serializer = new JsonJobSerializer();
    }

    @Test
    public void testSerialization() {
        JobWithData job = new JobWithData("a string");
        String payload = serializer.serialize(job);
        assertEquals("{\"data\":\"a string\"}", payload);
    }

    @Test
    public void testLambdaSerializationFailure() {
        assertThrows(IllegalArgumentException.class, () -> serializer.serialize(ctx -> {}));
    }

    @Test
    public void testDeserialization() {
        JobWithData job = serializer.deserialize(JobWithData.class, "{\"data\":\"a string\"}");
        assertEquals("a string", job.getData());
    }

}
