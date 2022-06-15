package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Action",
        description = "action"
)
public class Action {

    @LuaWhitelist
    @LuaFieldDoc(description = "action.left_action")
    public LuaFunction leftAction;
    @LuaWhitelist
    @LuaFieldDoc(description = "action.right_action")
    public LuaFunction rightAction;

    @LuaWhitelist
    @LuaFieldDoc(description = "action.title")
    public String title;

    public ItemStack item;
    public FiguraVec3 color;

    public Action(String title, FiguraVec3 color, ItemStack item, LuaFunction leftAction, LuaFunction rightAction) {
        this.title = title;
        this.color = color;
        this.item = item;
        this.leftAction = leftAction;
        this.rightAction = rightAction;
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
            description = "action.set_color"
    )
    public static void setColor(@LuaNotNil Action action, Object x, Double y, Double z) {
        action.color = LuaUtils.parseVec3("setColor", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Action.class,
                            argumentNames = "action"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, ItemStackWrapper.class},
                            argumentNames = {"action", "item"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Action.class, String.class},
                            argumentNames = {"action", "item"}
                    )
            },
            description = "action.set_item"
    )
    public static void setItem(@LuaNotNil Action action, Object item) {
        ItemStack itemStack = LuaUtils.parseItemStack("setItem", item);
    }

    @Override
    public String toString() {
        return "Action Wheel Action (" + title + ")";
    }
}
