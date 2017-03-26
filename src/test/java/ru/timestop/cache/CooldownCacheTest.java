package ru.timestop.cache;

import org.junit.Assert;
import org.junit.Test;
import ru.timestop.cache.cleaner.CooldownedCleaner;
import ru.timestop.cache.exception.CacheFullException;
import ru.timestop.cache.level.SimpleCacheLevel;

/**
 * Created by timestop on 29.03.17.
 */
public class CooldownCacheTest {

    @Test
    public void testCleanExpired() {
        MemoryCache<Integer, Integer> cacheA = new MemoryCache<>();
        MemoryCache<Integer, Integer> cacheB = new MemoryCache<>();
        SimpleCacheLevel<Integer, Integer> levelB = new SimpleCacheLevel<>(cacheB, 100);
        SimpleCacheLevel<Integer, Integer> levelA = new SimpleCacheLevel<>(cacheA, levelB, 100);

        Cache<Integer, Integer> cache = new CooldownedCleaner<>(levelA, 1000);

        int i;
        for (i = 0; i < 10; i++) {
            Integer k = i;
            cache.put(k, k);
        }
        try {
            Thread.sleep(1050);
        } catch (InterruptedException e) {
            Assert.fail("Sleep interrupted.");
        }
        Assert.assertTrue(levelB.getSize() == 0);
        Assert.assertTrue(levelA.getSize() == 10);
        for (; i < 110; i++) {
            Integer k = i;
            cache.put(k, k);
        }
        Assert.assertEquals("Chache level B has invalid elements", 100, levelA.getSize());
        Assert.assertEquals("Chache level A has invalid elements", 10, levelB.getSize());
        try {
            Integer k = i;
            cache.put(k, k);
            Assert.fail("Cache is full but no exception throws");
        } catch (CacheFullException e) {
            //SKIP
        }
        cache.clear();
    }
}
