package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.providers.StringProvider;
import org.figuramc.figura.lua.api.data.readers.StringReader;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@LuaWhitelist
@LuaTypeDoc(name = "DataAPI", value = "data")
public class DataAPI {
    @LuaFieldDoc("data.readers")
    public static final Readers readers = new Readers();
    @LuaFieldDoc("data.providers")
    public static final Providers providers = new Providers();

    @LuaFieldDoc("data.stream_reader")
    public static final StreamReader streamReader = new StreamReader();
    @LuaFieldDoc("data.stream_provider")
    public static final StreamReader streamWriter = new StreamReader();

    @LuaWhitelist
    @LuaMethodDoc(
            value = "data.create_buffer_input_stream",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "base64",
                            returnType = FiguraInputStream.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = LuaTable.class,
                            argumentNames = "byte_table",
                            returnType = FiguraInputStream.class
                    )
            }
    )
    public static FiguraInputStream createBufferInputStream(Object data) {
        byte[] streamData;
        if (data instanceof String) {
            streamData = Base64.getDecoder().decode((String) data);
        }
        else if (data instanceof LuaTable tbl) {
            streamData = new byte[tbl.length()];
            for(int i = 0; i < streamData.length; i++)
                streamData[i] = (byte) tbl.get(i + 1).checkint();
        }
        else throw new LuaError("Invalid data type");

        return new FiguraInputStream(new ByteArrayInputStream(streamData));
    }

    @LuaWhitelist
    @LuaTypeDoc(name = "Readers", value = "data_readers")
    public static class Readers {
        private Readers() {}
        @LuaFieldDoc("data_readers.string")
        public static final StringReader.Readers string = StringReader.READERS;
        @LuaWhitelist
        public Object __index(LuaValue key) {
            if (!key.isstring()) return null;
            return switch (key.tojstring()) {
                case "string" -> string;
                default -> null;
            };
        }
    }

    @LuaWhitelist
    @LuaTypeDoc(name = "Providers", value = "data_providers")
    public static class Providers {
        private Providers() {}
        @LuaFieldDoc("data_providers.string")
        public static final StringProvider.Providers string = StringProvider.PROVIDERS;
        @LuaWhitelist
        public Object __index(LuaValue key) {
            if (!key.isstring()) return null;
            return switch (key.tojstring()) {
                case "string" -> string;
                default -> null;
            };
        }
    }

    @LuaWhitelist
    public Object __index(LuaValue key) {
        if (!key.isstring()) return null;
        return switch (key.tojstring()) {
            case "readers" -> readers;
            case "providers" -> providers;
            case "streamReader" -> streamReader;
            case "streamWriter" -> streamWriter;
            default -> null;
        };
    }
}
