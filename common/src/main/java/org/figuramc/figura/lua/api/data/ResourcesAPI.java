package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@LuaWhitelist
@LuaTypeDoc(name = "ResourcesAPI", value = "resources")
public class ResourcesAPI {
    private final Avatar parent;

    public ResourcesAPI(Avatar parent) {
        this.parent = parent;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "resources.get",
            overloads = @LuaMethodOverload (
                    returnType = FiguraInputStream.class,
                    argumentTypes = String.class,
                    argumentNames = "path"
            )
    )
    public FiguraInputStream get(@LuaNotNil String path) {
        try {
            if (parent.resources.containsKey(path)) {
                ByteArrayInputStream bais = new ByteArrayInputStream(parent.resources.get(path));
                return new FiguraInputStream(new GZIPInputStream(bais));
            }
        } catch (IOException e) {
            throw new LuaError(e);
        }
        return null;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "resources.get_paths",
            overloads = @LuaMethodOverload (
                    returnType = LuaTable.class
            )
    )
    public List<String> getPaths() {
        return new ArrayList<>(parent.resources.keySet());
    }

    @LuaWhitelist
    public FiguraInputStream __index(@LuaNotNil String path) {
        return get(path);
    }

    @Override
    public String toString() {
        return "ResourcesAPI";
    }
}
