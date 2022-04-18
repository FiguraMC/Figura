package org.moon.figura.utils.caching;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class CacheUtils {

    public static <T extends CachedType> Cache<T> getCache(Supplier<T> generator) {
        return new Cache<>(generator);
    }

    public static class Cache<T extends CachedType> {
        private final Queue<T> cache;
        private final Supplier<T> generator;

        private Cache(Supplier<T> generator) {
            cache = new LinkedList<>();
            this.generator = generator;
        }

        public T getFresh() {
            T result = cache.poll();
            if (result == null)
                result = generator.get();
            result.reset();
            return result;
        }

        public void offerOld(T old) {
            cache.offer(old);
        }
    }
}
