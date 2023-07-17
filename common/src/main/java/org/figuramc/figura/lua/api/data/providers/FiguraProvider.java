package org.figuramc.figura.lua.api.data.providers;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "Provider",
        value = "provider"
)
public abstract class FiguraProvider<T> {
    @LuaWhitelist
    @LuaMethodDoc("provider.get_stream")
    public abstract FiguraInputStream getStream(T data);
}
