package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaType(typeName = "nameplate")
@LuaTypeDoc(
        name = "NameplateAPI",
        description = "nameplate"
)
public class NameplateAPI {

    @LuaFieldDoc(description = "nameplate.chat")
    public final NameplateCustomization CHAT;
    @LuaFieldDoc(description = "nameplate.entity")
    public final NameplateCustomization ENTITY;
    @LuaFieldDoc(description = "nameplate.list")
    public final NameplateCustomization LIST;

    public NameplateAPI() {
        CHAT = new NameplateCustomization();
        ENTITY = new NameplateCustomization();
        LIST = new NameplateCustomization();
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return switch (arg) {
            case "CHAT" -> CHAT;
            case "ENTITY" -> ENTITY;
            case "LIST" -> LIST;
            default -> null;
        };
    }
}
