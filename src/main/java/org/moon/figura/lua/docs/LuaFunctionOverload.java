package org.moon.figura.lua.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaFunctionOverload {

    /**
     * The types of the arguments in this overload.
     */
    Class<?>[] argumentTypes();

    /**
     * The names to give to the arguments in this overload.
     */
    String[] argumentNames();

    /**
     * The return type for this overload.
     */
    Class<?> returnType();
}
