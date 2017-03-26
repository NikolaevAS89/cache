package ru.timestop.cache.cleaner;

import ru.timestop.cache.level.CacheLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorator for {@link ru.timestop.cache.level.CacheLevel}
 * for move old object from cache level to subcache level.
 * For each key set cooldown time when it put and get.
 * When level is full keys with cooldown time earlier then
 * current time regarded as expired and for it call
 * {@link CooldownedCleaner#cleanCache(List)}.
 *
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 28.03.17
 */
public class CooldownedCleaner<K extends Serializable, V extends Serializable> implements CacheLevel<K, V> {

    private long cooldown;

    private final CacheLevel<K, V> cacheLevel;

    private final Map<K, Long> expired = new ConcurrentHashMap<>();
    private final Object lock;

    public CooldownedCleaner(CacheLevel<K, V> cacheLevel, long cooldown) {
        this.cacheLevel = cacheLevel;
        this.cooldown = cooldown;
        this.lock = new Object();
    }

    @Override
    public CacheLevel<K, V> getSubcache() {
        return cacheLevel.getSubcache();
    }

    @Override
    public boolean cleanCache(List<K> keys) {
        return cacheLevel.cleanCache(keys);
    }

    @Override
    public boolean isCacheFull() {
        return cacheLevel.isCacheFull();
    }

    @Override
    public V get(K key) {
        expired.put(key, System.currentTimeMillis() + cooldown);
        return cacheLevel.get(key);
    }

    @Override
    public synchronized V remove(K key) {
        expired.remove(key);
        return cacheLevel.remove(key);
    }

    @Override
    public void clear() {
        expired.clear();
        cacheLevel.clear();
    }

    @Override
    public void put(K key, V value) {
        if (cacheLevel.isCacheFull()) {
            synchronized (lock) {
                if (cacheLevel.isCacheFull()) {
                    cleanCache(getExpiredKeys());
                }
            }
        }
        expired.put(key, System.currentTimeMillis() + cooldown);
        cacheLevel.put(key, value);
    }

    @Override
    public int getSize() {
        return cacheLevel.getSize();
    }

    /**
     * @return expired keys of object
     */
    private List<K> getExpiredKeys() {
        Long currtime = System.currentTimeMillis();
        List<K> result = new ArrayList<>();
        for (Map.Entry<K, Long> entry : expired.entrySet()) {
            if (entry.getValue().compareTo(currtime) <= 0) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
