package org.moon.figura.lua.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation for a method that we add to lua
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaMethodDoc {
    /**
     * Each ParameterList should indicate one set of variable types that can
     * be used to call this method in a valid way.
     */
    LuaParameterList[] parameterTypeOptions();

    /**
     * A description of what this method does with its parameters.
     */
    String description();

    /**
     * The return type of this method.
     */
    Class<?> returnType();

}

