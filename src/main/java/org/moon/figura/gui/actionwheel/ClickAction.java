package org.moon.figura.gui.actionwheel;

import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;

@LuaWhitelist
@LuaTypeDoc(
        name = "ClickAction",
        description = "click_action"
)
public class ClickAction extends Action {

    // fields for funni lua syntax

    @LuaWhitelist
    @LuaFieldDoc(description = "click_action.left_click")
    public LuaFunction leftClick;

    @LuaWhitelist
    @LuaFieldDoc(description = "click_action.right_click")
    public LuaFunction rightClick;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {ClickAction.class, LuaFunction.class},
                    argumentNames = {"action", "leftFunction"}
            ),
            description = "click_action.on_left_click"
    )
    public static Action onLeftClick(ClickAction action, LuaFunction function) {
        action.leftClick = function;
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {ClickAction.class, LuaFunction.class},
                    argumentNames = {"action", "rightFunction"}
            ),
            description = "click_action.on_right_click"
    )
    public static Action onRightClick(ClickAction action, LuaFunction function) {
        action.rightClick = function;
        return action;
    }

    @Override
    public void execute(Avatar avatar, boolean left) {
        LuaFunction function = left ? leftClick : rightClick;

        //execute
        if (function != null)
            avatar.tryCall(function, -1);
    }

    @Override
    public String toString() {
        return "Action Wheel Click Action (" + title + ")";
    }
}
