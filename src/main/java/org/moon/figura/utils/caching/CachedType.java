package org.moon.figura.utils.caching;

public interface CachedType<T extends CachedType<T>> {
    T reset();
    void free();
}
