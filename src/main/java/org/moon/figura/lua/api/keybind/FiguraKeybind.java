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
    private final boolean ignoreScreen;

    private InputConstants.Key key;
    private boolean isDown = false;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.on_press")
    public LuaFunction onPress;

    @LuaWhitelist
    @LuaFieldDoc(description = "keybind.on_release")
    public LuaFunction onRelease;

    public FiguraKeybind(Avatar owner, String name, InputConstants.Key key, boolean ignoreScreen) {
        this.owner = owner;
        this.name = name;
        this.defaultKey = key;
        this.key = key;
        this.ignoreScreen = ignoreScreen;
    }

    public static void set(List<FiguraKeybind> bindings, InputConstants.Key key, boolean pressed) {
        FiguraKeybind keyBinding = null;
        for (FiguraKeybind keybind : bindings)
            if (keybind.key == key) {
                keyBinding = keybind;
                break;
            }

        if (keyBinding != null)
            setDown(keyBinding, pressed);
    }

    public static void releaseAll(List<FiguraKeybind> bindings) {
        for (FiguraKeybind keybind : bindings)
            setDown(keybind, false);
    }

    public static InputConstants.Key getDefaultKey(FiguraKeybind keybind) {
        return keybind.defaultKey;
    }

    public static void setDown(FiguraKeybind keybind, boolean bl) {
        //events
        if (keybind.isDown != bl) {
            if (bl) {
                if (keybind.onPress != null)
                    keybind.owner.tryCall(keybind.onPress, -1, keybind);
            } else if (keybind.onRelease != null) {
                keybind.owner.tryCall(keybind.onRelease, -1, keybind);
            }
        }

        keybind.isDown = bl;
    }

    public static Component getTranslatedKeyMessage(FiguraKeybind keybind) {
        return keybind.key.getDisplayName();
    }

    public static InputConstants.Key parseStringKey(String key) {
        try {
            return InputConstants.getKey(key);
        } catch (Exception ignored) {
            return InputConstants.Type.KEYSYM.getOrCreate(-1);
        }
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
        return (keybind.ignoreScreen || Minecraft.getInstance().screen == null) && keybind.isDown;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraKeybind.class, LuaFunction.class},
                    argumentNames = {"keybind", "function"}
            ),
            description = "keybind.set_on_press"
    )
    public static void setOnPress(@LuaNotNil FiguraKeybind keybind, @LuaNotNil LuaFunction function) {
        keybind.onPress = function;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraKeybind.class, LuaFunction.class},
                    argumentNames = {"keybind", "function"}
            ),
            description = "keybind.set_on_release"
    )
    public static void setOnRelease(@LuaNotNil FiguraKeybind keybind, @LuaNotNil LuaFunction function) {
        keybind.onRelease = function;
    }

    @Override
    public String toString() {
        return this.name + " (" + key.getName() + ") (Keybind)";
    }
}
