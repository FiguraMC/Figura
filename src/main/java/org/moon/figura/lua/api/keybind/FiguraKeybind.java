package org.moon.figura.lua.api.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Keybind",
        description = "keybind"
)
public class FiguraKeybind {

    private final Avatar owner;
    private final String name;
    private final InputConstants.Key defaultKey;

    private InputConstants.Key key;
    private boolean isDown = false;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.on_press")
    public LuaFunction onPress;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.on_release")
    public LuaFunction onRelease;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.enabled")
    public Boolean enabled = true;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.gui")
    public Boolean gui;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.override")
    public Boolean override;

    public FiguraKeybind(Avatar owner, String name, InputConstants.Key key) {
        this.owner = owner;
        this.name = name;
        this.defaultKey = key;
        this.key = key;
    }

    public void resetDefaultKey() {
        this.key = this.defaultKey;
    }

    public void setDown(boolean bl) {
        //events
        if (isDown != bl) {
            if (bl) {
                if (onPress != null)
                    owner.tryCall(onPress, -1, this);
            } else if (onRelease != null) {
                owner.tryCall(onRelease, -1, this);
            }
        }

        this.isDown = bl;
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
            if (keybind.key == key && keybind.enabled != null && keybind.enabled && ((keybind.gui != null && keybind.gui) || Minecraft.getInstance().screen == null)) {
                keybind.setDown(pressed);
                overrided = overrided || (keybind.override != null && keybind.override);
            }
        }
        return overrided;
    }

    public static void releaseAll(List<FiguraKeybind> bindings) {
        for (FiguraKeybind keybind : bindings)
            keybind.setDown(false);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "key"
            ),
            description = "keybind.set_key"
    )
    public void setKey(@LuaNotNil String key) {
        this.key = parseStringKey(key);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "keybind.is_default")
    public boolean isDefault() {
        return this.key.equals(this.defaultKey);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "keybind.get_key")
    public String getKey() {
        return this.key.getName();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "keybind.get_key_name")
    public String getKeyName() {
        return this.key.getDisplayName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "keybind.get_name")
    public String getName() {
        return this.name;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "keybind.is_pressed")
    public boolean isPressed() {
        return ((this.gui != null && this.gui) || Minecraft.getInstance().screen == null) && this.isDown;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "onPress" -> onPress;
            case "onRelease" -> onRelease;
            case "enabled" -> enabled;
            case "gui" -> gui;
            case "override" -> override;
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
            case "override" -> override = bool;
        }
    }

    @Override
    public String toString() {
        return this.name + " (" + key.getName() + ") (Keybind)";
    }
}
