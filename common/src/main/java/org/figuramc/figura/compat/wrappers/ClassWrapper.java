package org.figuramc.figura.compat.wrappers;

public class ClassWrapper {
    private final Class<?> clazz;
    public final boolean isLoaded;

    public ClassWrapper(String classpath) {
        Class<?> temp;
        boolean exists = false;
        try {
            temp = Class.forName(classpath);
            exists = true;
        } catch (Exception e) {
            temp = null;
        }

        clazz = temp;
        isLoaded = exists;
    }

    public MethodWrapper getMethod(String name, Class<?>... args) {
        return new MethodWrapper(clazz, name, args);
    }

    public FieldWrapper getField(String name) {
        return new FieldWrapper(clazz, name);
    }

}
