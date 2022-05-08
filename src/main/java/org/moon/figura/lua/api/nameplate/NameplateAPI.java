package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.types.LuaPairsIterator;

import java.util.List;

@LuaWhitelist
public class NameplateAPI {

    @LuaWhitelist
    public final NameplateCustomization CHAT;

    @LuaWhitelist
    public final NameplateCustomization ENTITY;

    @LuaWhitelist
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
}
