package org.moon.figura.lua.api.action_wheel;

import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "Page",
        value = "wheel_page"
)
public class Page {

    public Action[] actions = new Action[8]; //max 8 actions per page

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
            throw new LuaError("Reached page limit of 8 actions!");

        return index;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    )
            },
            value = "wheel_page.new_action"
    )
    public Action newAction(Integer index) {
        //set the action
        Action action = new Action();
        this.actions[this.checkIndex(index)] = action;

        //return the action
        return action;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "index"
            ),
            value = "wheel_page.get_action"
    )
    public Action getAction(int index) {
        if (index < 1 || index > 8)
            throw new LuaError("Index must be between 1 and 8!");
        return this.actions[index - 1];
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Action.class},
                    argumentNames = {"index", "action"}
            ),
            value = "wheel_page.set_action"
    )
    public void setAction(int index, Action action) {
        if (index == -1)
            index = this.checkIndex(null) + 1; //"why just not accept null" you might say, because -1 is more elegant for this, as it will return the latest available index
        else if (index < 1 || index > 8)
            throw new LuaError("Index must be between 1 and 8!");
        this.actions[index - 1] = action;
    }

    @Override
    public String toString() {
        return "Action Wheel Page";
    }
}
