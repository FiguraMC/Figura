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
        name = "ScrollAction",
        description = "scroll_action"
)
public class ScrollAction extends Action {

    @LuaWhitelist
    @LuaFieldDoc(description = "scroll_action.scroll")
    public LuaFunction scroll;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "scrollFunction"
            ),
            description = "scroll_action.on_scroll"
    )
    public Action onScroll(LuaFunction function) {
        this.scroll = function;
        return this;
    }

    @Override
    public void mouseScroll(Avatar avatar, double delta) {
        //execute
        if (scroll != null)
            avatar.tryCall(scroll, -1, delta);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        if ("scroll".equals(arg))
            return scroll;
        return null;
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        if (key == null) return;
        LuaFunction func = value instanceof LuaFunction f ? f : null;
        if ("scroll".equals(key))
            scroll = func;
    }

    @Override
    public String toString() {
        return title == null ? "Action Wheel Scroll" : "Action Wheel Scroll (" + title + ")";
    }
}
