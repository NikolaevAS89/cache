package ru.timestop.cache.bucket;

import ru.timestop.cache.Cache;
import ru.timestop.cache.exception.BucketException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 20.08.2018
 */
public class BucketedCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {

    private final List<Cache<K, V>> buckets;
    private final int bucketSize;
    private final AtomicInteger cnt;

    public BucketedCache(int bucketNum, BucketFactory<K, V> factory) {
        if (bucketNum <= 0) {
            throw new BucketException("Illegal buckets number");
        }
        cnt = new AtomicInteger(0);
        buckets = new ArrayList<>(bucketNum);
        bucketSize = Integer.MIN_VALUE / buckets.size();
        for (int i = 0; i < bucketNum; i++) {
            buckets.add(factory.build());
        }
    }

    @Override
    public void put(K key, V value) {
        cnt.incrementAndGet();
        getBucket(key).put(key, value);
    }

    @Override
    public V get(K key) {
        return getBucket(key).get(key);
    }

    @Override
    public V remove(K key) {
        V value = getBucket(key).remove(key);
        if (value != null) {
            cnt.decrementAndGet();
        }
        return value;
    }

    @Override
    public void clear() {
        cnt.set(0);
        for (Cache<K, V> bucket : buckets) {
            bucket.clear();
        }
    }

    @Override
    public int getSize() {
        return cnt.get();
    }

    /**
     * @param key
     * @return bucket by key hash
     */
    private Cache<K, V> getBucket(K key) {
        if (buckets.size() == 1) {
            return buckets.get(0);
        }
        int hash = key.hashCode();
        hash = (hash > 0) ? (-hash) : (hash);
        int pos = hash / bucketSize;
        pos = pos >= buckets.size() ? (buckets.size() - 1) : pos;
        return buckets.get(pos);
    }
}
