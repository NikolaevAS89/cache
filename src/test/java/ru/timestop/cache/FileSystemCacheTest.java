package ru.timestop.cache;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by timestop on 26.03.17.
 */


public class FileSystemCacheTest {

    @Test
    public void testCommonCacheWork() {
        FileCache<String, String> cache = new FileCache<>("cache");
        Assert.assertTrue("Some values not cached", cache.getSize() == 0);
        cache.put("Key1", "Value1");
        cache.put("Key2", "Value2");
        Assert.assertTrue("Some values not cached", cache.getSize() == 2);
        Assert.assertEquals(cache.get("Key1"), "Value1");
        cache.remove("Key1");
        Assert.assertTrue("Some values not cached", cache.getSize() == 1);
        Assert.assertEquals(cache.get("Key1"), null);
        Assert.assertEquals(cache.get("Key2"), "Value2");
        cache.clear();
        Assert.assertTrue("Some values not cached", cache.getSize() == 0);
    }

    @Test
    public void testCacheWorkWithBadKeys() {
        FileCache<BadHashKey, String> cache = new FileCache<>("cache");
        Assert.assertTrue("Some values not cached", cache.getSize() == 0);
        BadHashKey key1 = new BadHashKey();
        BadHashKey key2 = new BadHashKey();
        cache.put(key1, "Value1");
        cache.put(key2, "Value2");
        Assert.assertTrue("Some values not cached", cache.getSize() == 2);
        Assert.assertEquals(cache.get(key1), "Value1");
        cache.remove(key2);
        Assert.assertTrue("Some values not cached", cache.getSize() == 1);
        Assert.assertEquals(cache.get(key2), null);
        cache.clear();
        Assert.assertTrue("Some values not cached", cache.getSize() == 0);
    }

    @Test
    public void testEmpty() {
        FileCache<String, String> cache = new FileCache<>("cache");
        Assert.assertTrue("Some values not cached", cache.getSize() == 0);
        Assert.assertEquals(cache.get("Empty"), null);
        cache.clear();
        Assert.assertTrue("Some values not cached", cache.getSize() == 0);
    }

    /**
     * For test case when hash two or more keys are equals
     */
    private static class BadHashKey implements Serializable {

        private final String val = UUID.randomUUID().toString();

        public int hashCode() {
            return 1;
        }

        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(BadHashKey.class)) {
                return false;
            }
            return this.val.equals(((BadHashKey) obj).val);
        }
    }
}
