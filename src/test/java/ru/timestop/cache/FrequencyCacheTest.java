package ru.timestop.cache;

import org.junit.Assert;
import org.junit.Test;
import ru.timestop.cache.cleaner.FrequencyCleaner;
import ru.timestop.cache.exception.CacheFullException;
import ru.timestop.cache.level.SimpleCacheLevel;

/**
 * Created by timestop on 28.03.17.
 */
public class FrequencyCacheTest {
    @Test
    public void testCleanExpired() {
        MemoryCache<Integer, Integer> cacheA = new MemoryCache<>();
        MemoryCache<Integer, Integer> cacheB = new MemoryCache<>();
        SimpleCacheLevel<Integer, Integer> levelB = new SimpleCacheLevel<>(cacheB, 100);
        SimpleCacheLevel<Integer, Integer> levelA = new SimpleCacheLevel<>(cacheA, levelB, 100);

        Cache<Integer, Integer> cache = new FrequencyCleaner<>(levelA, 0.81f);

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
        for (; i < 119; i++) {
            Integer k = i;
            cache.put(k, k);
        }
        Assert.assertEquals("Chache level A has invalid elements", 100, levelA.getSize());
        Assert.assertEquals("Chache level B has invalid elements", 19, levelB.getSize());
        cache.clear();
    }
}
