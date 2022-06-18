package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Action",
        description = "wheel_action"
)
public abstract class Action {

    public String title;
    public ItemStack item;
    public FiguraVec3 color;
    public FiguraVec3 hoverColor;

    public void execute(Avatar avatar, boolean left) {}

    public void mouseScroll(Avatar avatar, double delta) {}

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Action.class, String.class},
                    argumentNames = {"action", "title"}
            ),
            description = "wheel_action.title"
    )
    public static Action title(Action action, String title) {
        action.title = title;
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, FiguraVec3.class},
                            argumentNames = {"action", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, Double.class, Double.class, Double.class},
                            argumentNames = {"action", "r", "g", "b"}
                    )
            },
            description = "wheel_action.color"
    )
    public static Action color(@LuaNotNil Action action, Object x, Double y, Double z) {
        action.color = LuaUtils.parseVec3("color", x, y, z);
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, FiguraVec3.class},
                            argumentNames = {"action", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, Double.class, Double.class, Double.class},
                            argumentNames = {"action", "r", "g", "b"}
                    )
            },
            description = "wheel_action.hover_color"
    )
    public static Action hoverColor(@LuaNotNil Action action, Object x, Double y, Double z) {
        action.hoverColor = LuaUtils.parseVec3("hoverColor", x, y, z);
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, ItemStackWrapper.class},
                            argumentNames = {"action", "item"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, String.class},
                            argumentNames = {"action", "item"}
                    )
            },
            description = "wheel_action.item"
    )
    public static Action item(@LuaNotNil Action action, Object item) {
        action.item = LuaUtils.parseItemStack("item", item);
        return action;
    }

    @Override
    public String toString() {
        return "Action Wheel Action (" + title + ")";
    }
}
