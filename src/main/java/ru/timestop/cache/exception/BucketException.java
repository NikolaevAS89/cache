package ru.timestop.cache.exception;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 20.08.2018.
 */
public class BucketException extends RuntimeException {
    public BucketException(Throwable e) {
        super(e);
    }

    public BucketException(String msg) {
        super(msg);
    }
}
