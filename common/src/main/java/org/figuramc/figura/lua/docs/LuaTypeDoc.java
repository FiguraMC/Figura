package org.figuramc.figura.lua.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation for a type we add to lua
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LuaTypeDoc {

    /**
     * Returns a name to be used for this type.
     */
    String name();

    /**
     * Returns a translation key for the description of this type.
     */
    String value();

}
