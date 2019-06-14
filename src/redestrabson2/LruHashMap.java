package redestrabson2;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @param <K> key
 * @param <V> value
 */
@SuppressWarnings("serial")
final class LruHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    public LruHashMap(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry entry) {
        return size() > capacity;
    }

}
