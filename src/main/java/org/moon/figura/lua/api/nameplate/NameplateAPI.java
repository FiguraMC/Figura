package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;

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
}
