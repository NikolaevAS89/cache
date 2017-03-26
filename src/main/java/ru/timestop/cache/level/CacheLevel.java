package ru.timestop.cache.level;

import ru.timestop.cache.Cache;

import java.io.Serializable;
import java.util.List;

/**
 * Extended {@link Cache} for create hierarchy of caches.
 *
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 15.08.2018.
 */
public interface CacheLevel<K extends Serializable, V extends Serializable> extends Cache<K, V> {

    /**
     * @return retun subcache or null if subcache not set
     */
    CacheLevel<K, V> getSubcache();

    /**
     * move objects to sublevel cache or delete them if sublevel cache is null
     *
     * @param keys of removed objects
     * @return true if remove at least one
     */
    boolean cleanCache(List<K> keys);

    /**
     * @return true if cache level is full
     */
    boolean isCacheFull();

    /**
     * @return size of level cache without size of sub cache levels
     */
    int getSize();
}