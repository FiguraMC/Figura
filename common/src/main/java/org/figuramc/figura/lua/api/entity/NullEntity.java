package org.figuramc.figura.lua.api.entity;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(
        name = "NullEntity",
        value = "" // no docs, however the name field is required for the wrapper
)
public class NullEntity {

    public static final NullEntity INSTANCE = new NullEntity();

    protected NullEntity() {}

    @LuaWhitelist
    public boolean isLoaded() {
        return false;
    }

    @LuaWhitelist
    public Object __index(Object o) {
        throw new LuaError("Tried to access the Entity API before its initialization in the ENTITY_INIT event. To check if the entity exists, use \"<entity>:isLoaded()\"");
    }

    @Override
    public String toString() {
        return "Uninitialized Entity API";
    }
}