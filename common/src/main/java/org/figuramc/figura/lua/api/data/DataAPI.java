package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.providers.StringProvider;
import org.figuramc.figura.lua.api.data.readers.StringReader;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaValue;

@LuaWhitelist
@LuaTypeDoc(name = "DataAPI", value = "data")
public class DataAPI {

    private final Avatar parent;

    @LuaFieldDoc("data.readers")
    public static final Readers readers = new Readers();
    @LuaFieldDoc("data.providers")
    public static final Providers providers = new Providers();

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

    @LuaWhitelist
    @LuaTypeDoc(name = "Readers", value = "data.readers")
    public static class Readers {
        private Readers() {}
        @LuaFieldDoc("data.readers.string")
        public static final StringReader.Instances string = StringReader.INSTANCES;
        @LuaWhitelist
        public Object __index(LuaValue key) {
            if (!key.isstring()) return null;
            return switch (key.tojstring()) {
                case "string" -> string;
                default -> null;
            };
        }

        @Override
        public String toString() {
            return "Readers";
        }
    }

    @LuaWhitelist
    @LuaTypeDoc(name = "Providers", value = "data.providers")
    public static class Providers {
        private Providers() {}
        @LuaFieldDoc("data.providers.string")
        public static final StringProvider.Instances string = StringProvider.INSTANCES;
        @LuaWhitelist
        public Object __index(LuaValue key) {
            if (!key.isstring()) return null;
            return switch (key.tojstring()) {
                case "string" -> string;
                default -> null;
            };
        }

        @Override
        public String toString() {
            return "Providers";
        }
    }

    @LuaWhitelist
    public Object __index(LuaValue key) {
        if (!key.isstring()) return null;
        return switch (key.tojstring()) {
            case "readers" -> readers;
            case "providers" -> providers;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "DataAPI";
    }
}
