package org.figuramc.figura.lua.docs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface LuaMethodOverload {

    /**
     * The types of the arguments in this overload.
     */
    Class<?>[] argumentTypes() default {};

    /**
     * The names to give to the arguments in this overload.
     */
    String[] argumentNames() default {};

    Class<?> returnType() default DEFAULT.class;

    final class DEFAULT {
    }
}
