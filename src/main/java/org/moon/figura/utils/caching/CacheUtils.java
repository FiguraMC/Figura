package org.moon.figura.utils.caching;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class CacheUtils {

    public static final int DEFAULT_MAX_SIZE = 500;

    public static <T extends CachedType> Cache<T> getCache(Supplier<T> generator, int maxSize) {
        return new Cache<>(generator, maxSize);
    }

    public static <T extends CachedType> Cache<T> getCache(Supplier<T> generator) {
        return new Cache<>(generator, DEFAULT_MAX_SIZE  );
    }

    public static class Cache<T extends CachedType> {
        private final Queue<T> cache;
        private final Supplier<T> generator;
        private final int maxSize;

        private Cache(Supplier<T> generator) {
            this(generator, Integer.MAX_VALUE);
        }

        private Cache(Supplier<T> generator, int maxSize) {
            cache = new LinkedList<>();
            this.generator = generator;
            this.maxSize = maxSize;
        }

        public T getFresh() {
            T result = cache.poll();
            if (result == null)
                result = generator.get();
            result.reset();
            return result;
        }

        public void offerOld(T old) {
            if (cache.size() >= maxSize)
                return;
            cache.offer(old);
        }
    }
}
