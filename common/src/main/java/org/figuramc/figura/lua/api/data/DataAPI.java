package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(name = "DataAPI", value = "data")
public class DataAPI {

    private final Avatar parent;

    public DataAPI(Avatar parent) {
        this.parent = parent;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "data.create_buffer",
            overloads = {
                    @LuaMethodOverload(
                            returnType = FiguraBuffer.class
                    ),
                    @LuaMethodOverload(
                            returnType = FiguraBuffer.class,
                            argumentNames = "capacity",
                            argumentTypes = Integer.class
                    )
            }
    )
    public FiguraBuffer createBuffer(Integer len) {
        return len == null ? new FiguraBuffer(parent) : new FiguraBuffer(parent, len);
    }

    @Override
    public String toString() {
        return "DataAPI";
    }
}
