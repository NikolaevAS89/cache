package ru.timestop.cache.level;

import ru.timestop.cache.Cache;
import ru.timestop.cache.exception.CacheFullException;

import java.io.Serializable;
import java.util.List;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 15.08.2018.
 */
public class SimpleCacheLevel<K extends Serializable, V extends Serializable> implements CacheLevel<K, V> {

    private final int maxSize;
    private final Cache<K, V> cache;
    private final CacheLevel<K, V> subcache;

    public SimpleCacheLevel(Cache<K, V> cache) {
        this(cache, null);
    }

    public SimpleCacheLevel(Cache<K, V> cache, CacheLevel<K, V> subcache) {
        this(cache, subcache, Integer.MAX_VALUE);
    }

    public SimpleCacheLevel(Cache<K, V> cache, int maxSize) {
        this(cache, null, maxSize);
    }

    public SimpleCacheLevel(Cache<K, V> cache, CacheLevel<K, V> subcache, int maxSize) {
        this.cache = cache;
        this.subcache = subcache;
        this.maxSize = maxSize;
    }

    @Override
    public CacheLevel<K, V> getSubcache() {
        return subcache;
    }

    @Override
    public boolean cleanCache(List<K> keys) {
        boolean isCleaned = false;
        if (subcache != null) {
            synchronized (subcache) {
                for (K expiredKey : keys) {
                    V removed = cache.remove(expiredKey);
                    if (removed != null) {
                        subcache.put(expiredKey, removed);
                        isCleaned = true;
                    }
                }
            }
        } else {
            for (K expiredKey : keys) {
                if (cache.remove(expiredKey) != null) {
                    isCleaned = true;
                }
            }
        }
        return isCleaned;
    }

    @Override
    public boolean isCacheFull() {
        return cache.getSize()>=maxSize;
    }

    @Override
    public void put(K key, V value) {
        if(isCacheFull()){
            throw new CacheFullException();
        }
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        V result = cache.get(key);
        if (subcache != null && result == null) {
            synchronized (subcache) {
                return subcache.get(key);
            }
        }
        return result;
    }

    @Override
    public V remove(K key) {
        V result = cache.remove(key);
        if (subcache != null && result == null) {
            synchronized (subcache) {
                return subcache.remove(key);
            }
        }
        return result;
    }

    @Override
    public void clear() {
        cache.clear();
        if (subcache != null) {
            synchronized (subcache) {
                subcache.clear();
            }
        }
    }

    @Override
    public int getSize() {
        return cache.getSize();
    }
}