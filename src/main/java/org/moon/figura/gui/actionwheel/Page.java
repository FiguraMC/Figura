package org.moon.figura.gui.actionwheel;

import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaType(typeName = "wheel_page")
@LuaTypeDoc(
        name = "Page",
        description = "wheel_page"
)
public class Page {

    Action[] actions = new Action[8]; //max 8 actions per page

    public int getSize() {
        int i = actions.length;
        while (i > 0 && actions[i - 1] == null) {
            i--;
        }
        return Math.max(i, 2);
    }

    private int checkIndex(Integer index) {
        //check and fix index
        if (index != null) {
            if (index < 1 || index > 8)
                throw new LuaError("Index must be between 1 and 8!");

            return index - 1;
        }

        //if no index is given, get the first null slot
        index = -1;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] == null) {
                index = i;
                break;
            }
        }

        //if failed to find a null slot, that means the page is full
        if (index == -1)
            throw new LuaError("Pages have a limit of 8 actions!");

        return index;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    )
            },
            description = "wheel_page.new_action"
    )
    public Action newAction(Integer index) {
        //set the action
        Action action = new ClickAction();
        this.actions[this.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    )
            },
            description = "wheel_page.new_toggle"
    )
    public Action newToggle(Integer index) {
        //set the action
        Action action = new ToggleAction();
        this.actions[this.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    )
            },
            description = "wheel_page.new_scroll"
    )
    public Action newScroll(Integer index) {
        //set the action
        Action action = new ScrollAction();
        this.actions[this.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "index"
            ),
            description = "wheel_page.get_action"
    )
    public Action getAction(@LuaNotNil Integer index) {
        if (index < 1 || index > 8)
            throw new LuaError("Index must be between 1 and 8!");
        return this.actions[index - 1];
    }
}
