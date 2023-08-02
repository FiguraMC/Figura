package org.figuramc.figura.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.figuramc.figura.FiguraMod;

import java.util.function.Function;

public class JsonUtils {
    public static boolean validate(JsonObject object, String fieldName, Function<JsonElement, Boolean> validator) {
        return (object.has(fieldName) && validator.apply(object.get(fieldName)));
    }

    public static boolean validate(JsonObject object, String fieldName, Function<JsonElement, Boolean> validator, String warningMessage, Object... params) {
        if (!validate(object, fieldName, validator)) {
            FiguraMod.LOGGER.warn(warningMessage, params);
            return false;
        }
        return true;
    }

    public static int getIntOrDefault(JsonObject object, String fieldName, int fallback) {
        if (validate(object, fieldName, JsonElement::isJsonPrimitive)) {
            return object.get(fieldName).getAsInt();
        }
        return fallback;
    }
}
