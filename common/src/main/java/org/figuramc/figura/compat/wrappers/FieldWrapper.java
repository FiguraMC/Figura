package org.figuramc.figura.compat.wrappers;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class FieldWrapper {
    @Nullable
    private final Field field;
    private boolean isErrored;

    public FieldWrapper(Class<?> clazz, String fieldName) {
        Field temp = null;
        try {
            temp = clazz.getDeclaredField(fieldName);
            temp.setAccessible(true);
        } catch (Exception ignored) {

        }

        field = temp;

    }

    public void markErrored() {
        isErrored = true;
    }

    public boolean exists() {
        return field != null && !isErrored;
    }

    public @Nullable Object getValue(Object holder) {
        if (!exists()) return null;

        try {
            return field.get(holder);
        } catch (Exception e) {
            isErrored = true;
            return null;
        }
    }
}