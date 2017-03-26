package ru.timestop.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 16.08.2018
 */
public class MemoryCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {

    private final Map<K, V> cache = new ConcurrentHashMap<K, V>();

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) throws NoSuchElementException {
        return cache.get(key);
    }

    @Override
    public V remove(K key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }
}