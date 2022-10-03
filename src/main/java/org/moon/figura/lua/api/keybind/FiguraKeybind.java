package org.moon.figura.lua.api.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Keybind",
        value = "keybind"
)
public class FiguraKeybind {

    private final Avatar owner;
    private final String name;
    private final InputConstants.Key defaultKey;

    private InputConstants.Key key;
    private boolean isDown, override;

    @LuaWhitelist
    @LuaFieldDoc("keybind.on_press")
    public LuaFunction onPress;

    @LuaWhitelist
    @LuaFieldDoc("keybind.on_release")
    public LuaFunction onRelease;

    @LuaWhitelist
    @LuaFieldDoc("keybind.enabled")
    public boolean enabled = true;

    @LuaWhitelist
    @LuaFieldDoc("keybind.gui")
    public boolean gui;

    public FiguraKeybind(Avatar owner, String name, InputConstants.Key key) {
        this.owner = owner;
        this.name = name;
        this.defaultKey = key;
        this.key = key;
    }

    public void resetDefaultKey() {
        this.key = this.defaultKey;
    }

    public boolean setDown(boolean bl) {
        //events
        if (isDown != bl) {
            Varargs result = null;

            if (bl) {
                if (onPress != null)
                    result = owner.run(onPress, owner.tick, this);
            } else if (onRelease != null) {
                result = owner.run(onRelease, owner.tick, this);
            }

            override = result != null && result.arg(1).isboolean() && result.checkboolean(1);
        }

        this.isDown = bl;
        return override;
    }

    public void setKey(InputConstants.Key key) {
        this.key = key;
    }

    public Component getTranslatedKeyMessage() {
        return this.key.getDisplayName();
    }

    // -- static -- //

    public static InputConstants.Key parseStringKey(String key) {
        try {
            return InputConstants.getKey(key);
        } catch (Exception passed) {
            throw new LuaError("Invalid key: " + key);
        }
    }

    public static boolean set(List<FiguraKeybind> bindings, InputConstants.Key key, boolean pressed) {
        boolean overrided = false;
        for (FiguraKeybind keybind : bindings) {
            if (keybind.key == key && keybind.enabled && (keybind.gui || Minecraft.getInstance().screen == null))
                overrided = keybind.setDown(pressed) || overrided;
        }
        return overrided;
    }

    public static void releaseAll(List<FiguraKeybind> bindings) {
        for (FiguraKeybind keybind : bindings)
            keybind.setDown(false);
    }

    public static void updateAll(List<FiguraKeybind> bindings) {
        for (FiguraKeybind keybind : bindings) {
            int value = keybind.key.getValue();
            if (keybind.enabled && keybind.key.getType() == InputConstants.Type.KEYSYM && value != InputConstants.UNKNOWN.getValue())
                keybind.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), value));
        }
    }

    public static boolean overridesKey(List<FiguraKeybind> bindings, InputConstants.Key key) {
        for (FiguraKeybind binding : bindings)
            if (binding.key == key && binding.enabled && binding.override)
                return true;
        return false;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "key"
            ),
            value = "keybind.set_key"
    )
    public void setKey(@LuaNotNil String key) {
        this.key = parseStringKey(key);
    }

    @LuaWhitelist
    @LuaMethodDoc("keybind.is_default")
    public boolean isDefault() {
        return this.key.equals(this.defaultKey);
    }

    @LuaWhitelist
    @LuaMethodDoc("keybind.get_key")
    public String getKey() {
        return this.key.getName();
    }

    @LuaWhitelist
    @LuaMethodDoc("keybind.get_key_name")
    public String getKeyName() {
        return this.key.getDisplayName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc("keybind.get_name")
    public String getName() {
        return this.name;
    }

    @LuaWhitelist
    @LuaMethodDoc("keybind.is_pressed")
    public boolean isPressed() {
        return (this.gui || Minecraft.getInstance().screen == null) && this.isDown;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "onPress" -> onPress;
            case "onRelease" -> onRelease;
            case "enabled" -> enabled;
            case "gui" -> gui;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        if (key == null) return;
        LuaFunction func = value instanceof LuaFunction f ? f : null;
        boolean bool = value instanceof Boolean b ? b : false;
        switch (key) {
            case "onPress" -> onPress = func;
            case "onRelease" -> onRelease = func;
            case "enabled" -> enabled = bool;
            case "gui" -> gui = bool;
        }
    }

    @Override
    public String toString() {
        return this.name + " (" + key.getName() + ") (Keybind)";
    }
}
