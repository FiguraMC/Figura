package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaPairsIterator;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateAPI",
        description = "nameplate"
)
public class NameplateAPI {

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate.chat")
    public final NameplateCustomization CHAT;

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate.entity")
    public final NameplateCustomization ENTITY;

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate.list")
    public final NameplateCustomization LIST;

    public NameplateAPI() {
        CHAT = new NameplateCustomization();
        ENTITY = new NameplateCustomization();
        LIST = new NameplateCustomization();
    }

    @LuaWhitelist
    public static LuaPairsIterator<NameplateAPI, String> __pairs(NameplateAPI arg) {
        return PAIRS_ITERATOR;
    }
    private static final LuaPairsIterator<NameplateAPI, String> PAIRS_ITERATOR =
            new LuaPairsIterator<>(List.of("CHAT", "ENTITY", "LIST"), NameplateAPI.class, String.class);

    @Override
    public String toString() {
        return "NameplateAPI";
    }
}
