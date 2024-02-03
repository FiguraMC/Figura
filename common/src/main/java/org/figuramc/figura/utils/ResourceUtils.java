package org.figuramc.figura.utils;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.figuramc.figura.FiguraMod;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ResourceUtils {

    public static byte[] getResource(ResourceManager manager, ResourceLocation path) {
        try (InputStream is = manager.getResource(path).getInputStream()) {
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
        }
        return null;
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = Maps.newHashMap();

            @Override
            public R apply(T object, U object2) {
                return this.cache.computeIfAbsent(Pair.of(object, object2), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + biFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return new Function<T, R>(){
            private final Map<T, R> cache = Maps.newHashMap();

            @Override
            public R apply(T object) {
                return this.cache.computeIfAbsent(object, function);
            }

            public String toString() {
                return "memoize/1[function=" + function + ", size=" + this.cache.size() + "]";
            }
        };
    }
}
