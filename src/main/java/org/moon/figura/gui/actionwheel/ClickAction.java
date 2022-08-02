package org.moon.figura.gui.actionwheel;

import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

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
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            description = "click_action.on_left_click"
    )
    public Action onLeftClick(LuaFunction function) {
        this.leftClick = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            description = "click_action.on_right_click"
    )
    public Action onRightClick(LuaFunction function) {
        this.rightClick = function;
        return this;
    }

    @Override
    public void execute(Avatar avatar, boolean left) {
        LuaFunction function = left ? leftClick : rightClick;

        //execute
        if (function != null)
            avatar.tryCall(function, -1, null);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return switch (arg) {
            case "leftClick" -> leftClick;
            case "rightClick" -> rightClick;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        switch (key) {
            case "leftClick" -> leftClick = (LuaFunction) value;
            case "rightClick" -> rightClick = (LuaFunction) value;
        }
    }

    @Override
    public String toString() {
        return title == null ? "Action Wheel Click Action" : "Action Wheel Click Action (" + title + ")";
    }
}
