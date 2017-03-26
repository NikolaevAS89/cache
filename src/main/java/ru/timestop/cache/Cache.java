package ru.timestop.cache;

import java.io.Serializable;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 15.08.2018
 */
public interface Cache<K extends Serializable, V extends Serializable> {
    /**
     * put value to cache.
     *
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * try to get cached value by key
     *
     * @param key
     * @return cachet oject or null if absen
     */
    V get(K key);

    /**
     * remove cached value from cache by key
     *
     * @param key
     * @return removed Object or null if absen
     */
    V remove(K key);

    /**
     * remove all cached values from cache
     */
    void clear();

    /**
     * @return size cache
     */
    int getSize();
}