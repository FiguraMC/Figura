package org.moon.figura.lua.api.keybind;

import net.minecraft.client.KeyMapping;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.mixin.input.KeyMappingAccessor;

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
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {KeybindAPI.class, String.class, String.class},
                            argumentNames = {"api", "name", "key"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {KeybindAPI.class, String.class, String.class, Boolean.class},
                            argumentNames = {"api", "name", "key", "gui"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {KeybindAPI.class, String.class, String.class, Boolean.class, Boolean.class},
                            argumentNames = {"api", "name", "key", "gui", "override"}
                    )
            },
            description = "keybind_api.create"
    )
    public static FiguraKeybind create(@LuaNotNil KeybindAPI api, @LuaNotNil String name, @LuaNotNil String key, Boolean gui, Boolean override) {
        api.keyBindings.removeIf(binding -> FiguraKeybind.getName(binding).equals(name));

        FiguraKeybind binding = new FiguraKeybind(api.owner, name, FiguraKeybind.parseStringKey(key));
        binding.gui = gui;
        binding.override = override;

        api.keyBindings.add(binding);
        return binding;
    }


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {KeybindAPI.class, String.class},
                    argumentNames = {"api", "id"}
            ),
            description = "keybind_api.get_vanilla_key"
    )
    public static String getVanillaKey(@LuaNotNil KeybindAPI api, @LuaNotNil String id) {
        KeyMapping key = KeyMappingAccessor.getAll().get(id);
        return key == null ? null : key.saveString();
    }

    @Override
    public String toString() {
        return "KeybindAPI";
    }
}
