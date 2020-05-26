package net.tislib.htmlstore;

import java.util.HashMap;
import java.util.Map;

public class CachedContainer<V> {
    private final Map<Integer, V> map = new HashMap<>();
    private final Map<V, Integer> mapR = new HashMap<>();
    private Integer index = 0;

    public Integer store(V value) {
        Integer index = mapR.get(value);
        if (index == null) {
            index = put(value);
        }

        return index;
    }

    public Integer put(V value) {
        index++;
        map.put(index, value);
        mapR.put(value, index);

        return index;
    }

    public void put(Integer key, V value) {
        map.put(index, value);
        mapR.put(value, index);
    }

    public Integer getIndex(V value) {
        return mapR.get(value);
    }

    public V get(Integer key) {
        return map.get(key);
    }

    public Map<Integer, V> getMap() {
        return map;
    }
}
