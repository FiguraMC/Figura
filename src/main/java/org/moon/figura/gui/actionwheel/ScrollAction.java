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
                    argumentTypes = {ScrollAction.class, LuaFunction.class},
                    argumentNames = {"action", "scrollFunction"}
            ),
            description = "scroll_action.on_scroll"
    )
    public static Action onScroll(ScrollAction action, LuaFunction function) {
        action.scroll = function;
        return action;
    }

    @Override
    public void mouseScroll(Avatar avatar, double delta) {
        //execute
        if (scroll != null)
            avatar.tryCall(scroll, -1, delta);
    }

}
