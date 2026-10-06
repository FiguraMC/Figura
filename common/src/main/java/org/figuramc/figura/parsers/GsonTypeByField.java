package org.figuramc.figura.parsers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helper type for disambiguating objects based on one of their fields.
 *
 * @param <T> target type superclass
 */
public class GsonTypeByField<T> implements TypeAdapterFactory {
    private final Class<T> targetType;
    private final Function<JsonElement, @Nullable String> discriminator;
    private Class<? extends T> fallbackType = null;

    public GsonTypeByField(Class<T> targetType, Function<JsonElement, @Nullable String> discriminator) {
        this.targetType = targetType;
        this.discriminator = discriminator;
    }

    public GsonTypeByField(Class<T> targetType, String discriminatorField) {
        this(
                targetType, element -> {
                    JsonElement field = element.getAsJsonObject().get(discriminatorField);
                    if (field == null || !field.isJsonPrimitive() || !field.getAsJsonPrimitive().isString())
                        return null;
                    return field.getAsString();
                }
        );
    }

    private final Map<String, Class<? extends T>> understoodTypes = new HashMap<>();

    @Override
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type == null) return null;
        Class<?> klass = type.getRawType();
        if (!klass.equals(targetType)) return null;

        TypeAdapter<JsonElement> element = gson.getAdapter(JsonElement.class);
        Map<String, TypeAdapter<? extends T>> subtypes = new HashMap<>();
        for (Map.Entry<String, Class<? extends T>> entry : understoodTypes.entrySet()) {
            TypeAdapter<? extends T> delegate = gson.getDelegateAdapter(
                    this,
                    TypeToken.get(entry.getValue())
            );
            subtypes.put(entry.getKey(), delegate);
        }
        TypeAdapter<? extends T> fallbackDelegate = fallbackType != null ? gson.getDelegateAdapter(
                this,
                TypeToken.get(fallbackType)
        ) : null;

        //noinspection unchecked : T == R
        return (TypeAdapter<R>) new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) {
                throw new IllegalArgumentException("Cannot serialize elements using this adapter");
            }

            private T fallback(JsonElement contents, Exception reason) {
                if (fallbackDelegate == null) throw new JsonParseException("No match: " + reason.toString(), reason);
                return fallbackDelegate.fromJsonTree(contents);
            }

            private T fallback(JsonElement contents, String attempted) {
                if (fallbackDelegate == null) throw new JsonParseException("No match on '" + attempted + "'");
                return fallbackDelegate.fromJsonTree(contents);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement it = element.read(in);
                String type;
                try {
                    type = discriminator.apply(it);
                } catch (RuntimeException err) {
                    // probably ClassCastException (bad data)
                    return fallback(it, err);
                }

                if (type == null)
                    // well we sure don't know what it is, but _maybe_ we can survive?
                    // (in all likelihood this will fail to deserialize)
                    return fallback(it, "<unknown>");

                TypeAdapter<? extends T> delegate = subtypes.get(type);
                if (delegate == null)
                    // this might pass, if it's just a case of unsupported type
                    return fallback(it, type);
                return delegate.fromJsonTree(it);
            }
        };
    }

    public GsonTypeByField<T> bind(String value, Class<? extends T> target) {
        understoodTypes.put(value, target);
        return this;
    }

    public GsonTypeByField<T> withFallback(Class<? extends T> fallback) {
        this.fallbackType = fallback;
        return this;
    }
}
