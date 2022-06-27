package org.moon.figura.lua.api.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Keybind",
        description = "keybind"
)
public final class FiguraKeybind {

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
            throw new LuaRuntimeException("Invalid key: " + key);
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
                    argumentTypes = {FiguraKeybind.class, String.class},
                    argumentNames = {"keybind", "key"}
            ),
            description = "keybind.set_key"
    )
    public static void setKey(@LuaNotNil FiguraKeybind keybind, @LuaNotNil String key) {
        keybind.key = parseStringKey(key);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraKeybind.class,
                    argumentNames = "keybind"
            ),
            description = "keybind.is_default"
    )
    public static boolean isDefault(@LuaNotNil FiguraKeybind keybind) {
        return keybind.key.equals(keybind.defaultKey);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraKeybind.class,
                    argumentNames = "keybind"
            ),
            description = "keybind.get_key"
    )
    public static String getKey(@LuaNotNil FiguraKeybind keybind) {
        return keybind.key.getName();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraKeybind.class,
                    argumentNames = "keybind"
            ),
            description = "keybind.get_key_name"
    )
    public static String getKeyName(@LuaNotNil FiguraKeybind keybind) {
        return keybind.key.getDisplayName().getString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraKeybind.class,
                    argumentNames = "keybind"
            ),
            description = "keybind.get_name"
    )
    public static String getName(@LuaNotNil FiguraKeybind keybind) {
        return keybind.name;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraKeybind.class,
                    argumentNames = "keybind"
            ),
            description = "keybind.is_pressed"
    )
    public static boolean isPressed(@LuaNotNil FiguraKeybind keybind) {
        return ((keybind.gui != null && keybind.gui) || Minecraft.getInstance().screen == null) && keybind.isDown;
    }

    @Override
    public String toString() {
        return this.name + " (" + key.getName() + ") (Keybind)";
    }
}
