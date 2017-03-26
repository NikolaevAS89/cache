package ru.timestop.cache;

import ru.timestop.cache.bucket.BucketFactory;
import ru.timestop.cache.bucket.BucketedCache;
import ru.timestop.cache.cleaner.CooldownedCleaner;
import ru.timestop.cache.cleaner.FrequencyCleaner;
import ru.timestop.cache.level.CacheLevel;
import ru.timestop.cache.level.SimpleCacheLevel;

import java.io.Serializable;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 15.08.2018.
 */
public class TwoLevelCacheBuilder<K extends Serializable, V extends Serializable> {

    private String rootDirectoryB = "cache";
    private int bucketCntB = 4;
    private int maxSizeB = 1000;
    private float loadfactorB = 0.75f;
    private int maxSizeA = 100;
    private int cooldownA = 1000;
    private float loadfactorA = 0.75f;

    public TwoLevelCacheBuilder() {

    }

    public Cache<K, V> build() {
        // create file cache
        BucketedCache<K, V> cacheB = new BucketedCache<>(bucketCntB, new FileCacheFactory());
        CacheLevel<K, V> cacheLevelB = new SimpleCacheLevel<>(cacheB, maxSizeB);
        cacheLevelB = new FrequencyCleaner<>(cacheLevelB, loadfactorB);
        // create memory cache
        MemoryCache<K, V> cacheA = new MemoryCache<>();
        CacheLevel<K, V> cacheLevelA = new SimpleCacheLevel<>(cacheA, cacheLevelB, maxSizeA);
        cacheLevelA = new FrequencyCleaner<>(cacheLevelA, loadfactorA);
        cacheLevelA = new CooldownedCleaner<>(cacheLevelA, cooldownA);
        // return
        return cacheLevelA;
    }

    public TwoLevelCacheBuilder<K, V> setRootDirectoryB(String rootDirectoryB) {
        this.rootDirectoryB = rootDirectoryB;
        return this;
    }

    public TwoLevelCacheBuilder<K, V> setBucketCntB(int bucketCntB) {
        this.bucketCntB = bucketCntB;
        return this;
    }

    public TwoLevelCacheBuilder<K, V> setMaxSizeB(int maxSizeB) {
        this.maxSizeB = maxSizeB;
        return this;
    }

    public TwoLevelCacheBuilder<K, V> setMaxSizeA(int maxSizeA) {
        this.maxSizeA = maxSizeA;
        return this;
    }

    public TwoLevelCacheBuilder<K, V> setCooldownA(int cooldownA) {
        this.cooldownA = cooldownA;
        return this;
    }

    public TwoLevelCacheBuilder<K, V> setLoadfactorA(float loadfactorA) {
        this.loadfactorA = loadfactorA;
        return this;
    }

    public TwoLevelCacheBuilder<K, V> setLoadfactorB(float loadfactorB) {
        this.loadfactorB = loadfactorB;
        return this;
    }

    /**
     *
     */
    private class FileCacheFactory implements BucketFactory<K, V> {
        @Override
        public Cache<K, V> build() {
            return new FileCache<K, V>(rootDirectoryB);
        }
    }
}
