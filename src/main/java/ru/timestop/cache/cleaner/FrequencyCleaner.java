package ru.timestop.cache.cleaner;

import ru.timestop.cache.level.CacheLevel;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Decorator for {@link ru.timestop.cache.level.CacheLevel}
 * for move old object from cache level to subcache level.
 * For each key set frequency counter.
 * Counter increase when {@link CacheLevel#get(Serializable)} called for key.
 * When level is full keys with least counter regarded as expired and for it call
 * {@link CooldownedCleaner#cleanCache(List)}. Number of <b>not</b> expired keys
 * define {@link FrequencyCleaner#loadFactor}.
 *
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 15.08.2018.
 */
public class FrequencyCleaner<K extends Serializable, V extends Serializable> implements CacheLevel<K, V> {

    private final CacheLevel<K, V> cacheLevel;
    private final double loadFactor;
    private final Map<K, Integer> frequency = new ConcurrentHashMap<>();
    private final Object lock;

    public FrequencyCleaner(CacheLevel<K, V> cacheLevel, double loadFactor) {
        this.cacheLevel = cacheLevel;
        this.loadFactor = loadFactor;
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
    public void put(K key, V value) {
        if (cacheLevel.isCacheFull()) {
            synchronized (lock) {
                if (cacheLevel.isCacheFull()) {
                    cleanCache(getExpiredKeys());
                    frequency.entrySet().forEach(itm -> itm.setValue(0));
                }
            }
        }
        cacheLevel.put(key, value);
        frequency.put(key, 0);
    }

    @Override
    public V get(K key) {
        frequency.put(key, frequency.get(key) + 1);
        return cacheLevel.get(key);
    }

    @Override
    public V remove(K key) {
        frequency.remove(key);
        return cacheLevel.remove(key);
    }

    @Override
    public void clear() {
        frequency.clear();
        cacheLevel.clear();
    }

    @Override
    public int getSize() {
        return cacheLevel.getSize();
    }

    /**
     * @return expired keys of object
     */
    private List<K> getExpiredKeys() {
        List<Map.Entry<K, Integer>> freqs = new ArrayList<>(frequency.entrySet());
        int cleanCnt = freqs.size() - (int) (loadFactor * freqs.size());
        if (cleanCnt <= 0) {
            return Collections.emptyList();
        }
        Collections.sort(freqs, Comparator.comparing(Map.Entry::getValue));
        return freqs.stream()
                .limit(cleanCnt)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
