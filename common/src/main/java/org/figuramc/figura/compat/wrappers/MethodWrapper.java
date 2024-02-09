package org.figuramc.figura.compat.wrappers;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodWrapper {
    @Nullable
    private final Method method;
    private boolean isErrored;

    public MethodWrapper(Class<?> clazz, String methodName, Class<?>... args) {
        Method temp = null;
        try {
            temp = clazz.getDeclaredMethod(methodName, args);
            temp.setAccessible(true);
        } catch (Exception ignored) {

        }

        method = temp;

    }

    public void markErrored() {
        isErrored = true;
    }

    public boolean exists() {
        return method != null && !isErrored;
    }

    public @Nullable Object invoke(Object caller, Object... args) {
        if (!exists()) return null;

        try {
            return method.invoke(caller, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            isErrored = true;
            return null;
        }
    }
}