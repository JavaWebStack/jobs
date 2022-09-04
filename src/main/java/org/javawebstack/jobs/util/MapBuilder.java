package org.javawebstack.jobs.util;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K,V> {

    final Map<K, V> map;

    public MapBuilder() {
        this(new HashMap<>());
    }

    public MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    public MapBuilder<K, V> set(K k, V v) {
        map.put(k, v);
        return this;
    }

    public Map<K, V> build() {
        return map;
    }

}
