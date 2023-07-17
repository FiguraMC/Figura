package org.figuramc.figura.lua.api.data.readers;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.InputStream;

@LuaWhitelist
@LuaTypeDoc(name = "StringReader", value = "string_reader")
public class StringReader extends FiguraReader<String> {
    @Override
    @LuaWhitelist
    @LuaMethodDoc("reader.read_from")
    public String readFrom(InputStream stream) {
        return null;
    }
}
