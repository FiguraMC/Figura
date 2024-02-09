package org.figuramc.figura.lua.api.nameplate;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

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
        switch (arg.toUpperCase()) {
            case "CHAT":
                return CHAT;
            case "ENTITY":
                return ENTITY;
            case "LIST":
                return LIST;
            case "ALL":
                return ALL;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "NameplateAPI";
    }
}
