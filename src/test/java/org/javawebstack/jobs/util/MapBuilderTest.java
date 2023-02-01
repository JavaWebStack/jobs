package org.javawebstack.jobs.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MapBuilderTest {

    @Test
    public void testMapBuilding() {
        MapBuilder<String, String> builder = new MapBuilder<>();
        builder.set("hello", "world");
        Map<String, String> map = builder.build();
        assertEquals("world", map.get("hello"));
    }

}
