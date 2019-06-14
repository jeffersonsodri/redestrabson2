package redestrabson2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LRUCache<K, V> implements Cache<K, V> {

    /**
     * The flag represents remove all entries in the cache.
     */
    private static final int REMOVE_ALL = -1;

    private static final int DEFAULT_CAPACITY = 10;

    final Map<K, V> map;

    int maxMemorySize;

    private int memorySize;

    public LRUCache() {
        this(DEFAULT_CAPACITY);
    }

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        this.map = new LruHashMap<>(capacity);
        maxMemorySize = capacity * 1024 * 1024;
    }

    @Override
    public final V get(K key) {
        Objects.requireNonNull(key, "key == null");
        synchronized (this) {
            V value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(value, "value == null");
        V previous;
        synchronized (this) {
            previous = map.put(key, value);
            memorySize += getValueSize(value);
            if (previous != null) {
                memorySize -= getValueSize(previous);
            }
            trimToSize(maxMemorySize);
        }
        return previous;
    }

    @Override
    public final V remove(K key) {
        Objects.requireNonNull(key, "key == null");
        V previous;
        synchronized (this) {
            previous = map.remove(key);
            if (previous != null) {
                memorySize -= getValueSize(previous);
            }
        }
        return previous;
    }

    @Override
    public synchronized final void clear() {
        trimToSize(REMOVE_ALL);
    }

    @Override
    public synchronized final int getMaxMemorySize() {
        return maxMemorySize;
    }

    @Override
    public synchronized final int getMemorySize() {
        return memorySize;
    }

    /**
     * Retorna a compia do que esta contido na Cache
     */
    public synchronized final Map<K, V> snapshot() {
        return new LinkedHashMap<>(map);
    }

    /**
     * Retorna o nome da classe
     * <p>
     * @return nome da classe.
     */
    protected String getClassName() {
        return LRUCache.class.getName();
    }

    /**
     * Retorna o tamanho da entrada.
     * <p>
     *
     * @param value value
     * @return o tamanho da entrada.
     */
    protected long getValueSize(V value) {
    	return 1;
    }

    /**
     * Remove a entrada mais antiga.
     * <p>

     * @param maxSize tamanho maximo
     */
    private void trimToSize(int maxSize) {
        while (true) {
            if (memorySize <= maxSize || map.isEmpty()) {
                break;
            }
            if (memorySize < 0 || (map.isEmpty() && memorySize != 0)) {
                throw new IllegalStateException(getClassName() + ".getValueSize() este reportando valores inconsistentes");
            }
            Map.Entry<K, V> toRemove = map.entrySet().iterator().next();
            map.remove(toRemove.getKey());
            memorySize -= getValueSize(toRemove.getValue());
        }
    }

    @Override
    public synchronized final String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getKey())
                    .append('=')
                    .append(entry.getValue())
                    .append("\n");
        }
        sb.append("maxMemory=")
                .append(maxMemorySize)
                .append(",")
                .append("memorySize=")
                .append(memorySize);
        return sb.toString();
    }

}