package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.moon.figura.avatars.Avatar;
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
        name = "ToggleAction",
        description = "toggle_action"
)
public class ToggleAction extends Action {

    private boolean toggled = false;
    public ItemStack toggleItem;
    public FiguraVec3 toggleColor;

    @LuaWhitelist
    @LuaFieldDoc(description = "toggle_action.toggle")
    private LuaFunction toggle;

    @LuaWhitelist
    @LuaFieldDoc(description = "toggle_action.untoggle")
    private LuaFunction untoggle;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {ToggleAction.class, LuaFunction.class},
                    argumentNames = {"toggle", "leftFunction"}
            ),
            description = "toggle_action.on_toggle"
    )
    public static Action onToggle(ToggleAction action, LuaFunction function) {
        action.toggle = function;
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {ToggleAction.class, LuaFunction.class},
                    argumentNames = {"toggle", "rightFunction"}
            ),
            description = "toggle_action.on_untoggle"
    )
    public static Action onUntoggle(ToggleAction action, LuaFunction function) {
        action.untoggle = function;
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {ToggleAction.class, FiguraVec3.class},
                            argumentNames = {"toggle", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ToggleAction.class, Double.class, Double.class, Double.class},
                            argumentNames = {"toggle", "r", "g", "b"}
                    )
            },
            description = "toggle_action.toggle_color"
    )
    public static Action toggleColor(@LuaNotNil ToggleAction action, Object x, Double y, Double z) {
        action.toggleColor = LuaUtils.parseVec3("toggleColor", x, y, z);
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {ToggleAction.class, ItemStackWrapper.class},
                            argumentNames = {"action", "item"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ToggleAction.class, String.class},
                            argumentNames = {"action", "item"}
                    )
            },
            description = "toggle_action.toggle_item"
    )
    public static Action toggleItem(@LuaNotNil ToggleAction action, Object item) {
        action.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {ToggleAction.class, Boolean.class},
                    argumentNames = {"toggled", "bool"}
            ),
            description = "toggle_action.toggled"
    )
    public static void toggled(@LuaNotNil ToggleAction action, @LuaNotNil Boolean bool) {
        action.toggled = bool;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ToggleAction.class,
                    argumentNames = "toggle"
            ),
            description = "toggle_action.is_toggled"
    )
    public static boolean isToggled(@LuaNotNil ToggleAction action) {
        return action.toggled;
    }

    @Override
    public void execute(Avatar avatar, boolean left) {
        if (!left)
            return;

        toggled = !toggled;
        LuaFunction function = toggled ? toggle : untoggle;

        //execute
        if (function != null)
            avatar.tryCall(function, -1);
    }

    @Override
    public String toString() {
        return "Action Wheel Toggle (" + title + ")";
    }
}
