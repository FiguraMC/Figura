package org.figuramc.figura.lua.api.data.readers;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.InputStream;

@LuaWhitelist
@LuaTypeDoc(
        name = "Reader",
        value = "reader"
)
public abstract class FiguraReader<R> {
    @LuaWhitelist
    @LuaMethodDoc("reader.read_from")
    public abstract R readFrom(InputStream stream);
}
