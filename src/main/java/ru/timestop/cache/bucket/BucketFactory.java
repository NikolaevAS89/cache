package ru.timestop.cache.bucket;

import ru.timestop.cache.Cache;

import java.io.Serializable;

/**
 * Produce {@link ru.timestop.cache.Cache} for
 * build {@link ru.timestop.cache.bucket.BucketedCache}
 *
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 22.08.2018
 */
public interface BucketFactory<K extends Serializable, V extends Serializable> {

    /**
     * @return new {@link ru.timestop.cache.Cache}
     */
    Cache<K, V> build();
}
