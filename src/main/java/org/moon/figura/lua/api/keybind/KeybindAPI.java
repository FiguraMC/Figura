package org.moon.figura.lua.api.keybind;

import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.ArrayList;

@LuaWhitelist
@LuaTypeDoc(
        name = "KeybindAPI",
        description = "keybind_api"
)
public class KeybindAPI {

    public final ArrayList<FiguraKeybind> keyBindings = new ArrayList<>();
    public final Avatar owner;

    public KeybindAPI(Avatar owner) {
        this.owner = owner;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {KeybindAPI.class, String.class, String.class, Boolean.class},
                    argumentNames = {"api", "name", "key", "gui"}
            ),
            description = "keybind_api.create"
    )
    public static FiguraKeybind create(@LuaNotNil KeybindAPI api, @LuaNotNil String name, @LuaNotNil String key, Boolean gui) {
        api.keyBindings.removeIf(binding -> FiguraKeybind.getName(binding).equals(name));

        FiguraKeybind binding = new FiguraKeybind(api.owner, name, FiguraKeybind.parseStringKey(key), gui);
        api.keyBindings.add(binding);
        return binding;
    }

    @Override
    public String toString() {
        return "KeybindAPI";
    }
}
