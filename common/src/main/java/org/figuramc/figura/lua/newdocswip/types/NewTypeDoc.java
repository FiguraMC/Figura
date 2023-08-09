package org.figuramc.figura.lua.newdocswip.types;

import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Documentation for an object type that can be obtained in lua.
 * Generated from a class with the @LuaTypeDoc annotation on it.
 */
public class NewTypeDoc extends NewFiguraDoc<LuaTypeDoc> {

    /**
     * The class which this documentation describes.
     */
    private final Class<?> documentedClass;
    /**
     * A list of all methods available to the type.
     */
    private final List<NewMethodDoc> methods = new ArrayList<>();

    /**
     * A list of all metamethods available on the type.
     */
    private final List<NewMetamethodDoc> metamethods = new ArrayList<>();

    /**
     * A list of all fields available on the type.
     */
    private final List<NewFieldDoc> fields = new ArrayList<>();

    // Private constructor, prefer to use createForClass() instead.
    private NewTypeDoc(Class<?> clazz) {
        documentedClass = clazz;
    }

    /**
     * Creates a documentation object for the provided class.
     * @param clazz The class to document.
     * @return An instance of TypeDoc for that class.
     */
    public static NewTypeDoc createForClass(Class<?> clazz) {
        NewTypeDoc result = new NewTypeDoc(clazz);




        return result;
    }


}
