package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

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
    public final EntityNameplateCustomization ENTITY;
    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate.list")
    public final NameplateCustomization LIST;
    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate.all")
    public final NameplateCustomizationGroup ALL;

    public NameplateAPI() {
        CHAT = new NameplateCustomization();
        ENTITY = new EntityNameplateCustomization();
        LIST = new NameplateCustomization();
        ALL = new NameplateCustomizationGroup(CHAT, ENTITY, LIST);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return switch (arg) {
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
