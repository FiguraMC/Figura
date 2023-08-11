package org.figuramc.figura.lua.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LuaMetamethodDoc {

    /**
     * Each {@link LuaMetamethodOverload} should indicate one set of variable types that can
     * be used to call this method in a valid way.
     */
    LuaMetamethodOverload[] overloads();

    @Retention(RetentionPolicy.RUNTIME)
    @interface LuaMetamethodOverload {

        // First value is the result type, rest of values are the parameter types.
        Class<?>[] types();

        String comment() default "";
    }
}
