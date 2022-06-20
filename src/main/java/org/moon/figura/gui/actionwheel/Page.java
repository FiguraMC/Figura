package org.moon.figura.gui.actionwheel;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
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
                throw new LuaRuntimeException("Index must be between 1 and 8!");

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
            throw new LuaRuntimeException("Pages have a limit of 8 actions!");

        return index;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Page.class,
                            argumentNames = "page"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, Integer.class},
                            argumentNames = {"page", "index"}
                    )
            },
            description = "wheel_page.new_action"
    )
    public static Action newAction(@LuaNotNil Page page, Integer index) {
        //set the action
        Action action = new ClickAction();
        page.actions[page.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Page.class,
                            argumentNames = "page"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, Integer.class},
                            argumentNames = {"page", "index"}
                    )
            },
            description = "wheel_page.new_toggle"
    )
    public static Action newToggle(@LuaNotNil Page page, Integer index) {
        //set the action
        Action action = new ToggleAction();
        page.actions[page.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Page.class,
                            argumentNames = "page"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, Integer.class},
                            argumentNames = {"page", "index"}
                    )
            },
            description = "wheel_page.new_scroll"
    )
    public static Action newScroll(@LuaNotNil Page page, Integer index) {
        //set the action
        Action action = new ScrollAction();
        page.actions[page.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Page.class, Integer.class},
                    argumentNames = {"page", "index"}
            ),
            description = "wheel_page.get_action"
    )
    public static Action getAction(@LuaNotNil Page page, @LuaNotNil Integer index) {
        if (index < 1 || index > 8)
            throw new LuaRuntimeException("Index must be between 1 and 8!");
        return page.actions[index - 1];
    }

    @Override
    public String toString() {
        return "Action Wheel Page";
    }
}
