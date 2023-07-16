package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateAPI",
        value = "nameplate"
)
public class NameplateAPI {

    @LuaWhitelist
    @LuaFieldDoc("nameplate.chat")
    public final NameplateCustomization CHAT;
    @LuaWhitelist
    @LuaFieldDoc("nameplate.entity")
    public final EntityNameplateCustomization ENTITY;
    @LuaWhitelist
    @LuaFieldDoc("nameplate.list")
    public final NameplateCustomization LIST;
    @LuaWhitelist
    @LuaFieldDoc("nameplate.all")
    public final NameplateCustomizationGroup ALL;

    public NameplateAPI() {
        CHAT = new NameplateCustomization();
        ENTITY = new EntityNameplateCustomization();
        LIST = new NameplateCustomization();
        ALL = new NameplateCustomizationGroup(CHAT, ENTITY, LIST);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg.toUpperCase()) {
            case "CHAT" -> CHAT;
            case "ENTITY" -> ENTITY;
            case "LIST" -> LIST;
            case "ALL" -> ALL;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "NameplateAPI";
    }
}
