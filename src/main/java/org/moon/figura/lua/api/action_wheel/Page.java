package org.moon.figura.lua.api.action_wheel;

import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Page",
        value = "wheel_page"
)
public class Page {

    private final String title;

    private final HashMap<Integer, Action> actionsMap = new HashMap<>();

    private int groupIndex = 0;

    @LuaWhitelist
    @LuaFieldDoc("wheel_page.keep_last_group")
    public boolean keepLastGroup = false;

    public Page(String title) {
        this.title = title;
    }

    public int getSize() {
        Action[] actions = group();
        int i = actions.length;
        while (i > 0 && actions[i - 1] == null) {
            i--;
        }
        return Math.max(i, 2);
    }

    public int getGroupCount() {
        int greatest = 0;
        for (Integer i : actionsMap.keySet()) {
            greatest = i > greatest ? i : greatest;
        }
        return greatest / 8 + 1;
    }

    public Action[] group() {
        return group(groupIndex);
    }

    public Action[] group(int groupIndex) {
        Action[] page = new Action[8];
        for (int i = 0; i < 8; i++) {
            page[i] = actionsMap.get(i + 8 * groupIndex);
        }
        return page;
    }

    private int checkIndex(Integer index) {
        //check and fix index
        if (index != null) {
            if (index < 1)
                throw new LuaError("Index must be greater than 0!");

            return index - 1;
        }

        //if no index is given, get the first null slot
        int i = 0;
        while (actionsMap.get(i) != null)
            i++;

        return i;
    }


    // -- lua stuff -- //


    @LuaWhitelist
    @LuaMethodDoc("wheel_page.should_keep_last_group")
    public boolean shouldKeepLastGroup() {
        return keepLastGroup;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "keepLastGroup"
            ),
            value = "wheel_page.set_keep_last_group")
    public Page setKeepLastGroup(boolean bool) {
        keepLastGroup = bool;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_page.get_title")
    public String getTitle() {
        return title;
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
        this.actionsMap.put(checkIndex(index), action);

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
        if (index < 1)
            throw new LuaError("Index must be greater than 0!");
        return this.actionsMap.get(index - 1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Integer.class, Action.class},
                    argumentNames = {"index", "action"}
            ),
            value = "wheel_page.set_action"
    )
    public Page setAction(int index, Action action) {
        if (index < 1)
            throw new LuaError("Index must be greater than 0!");
        this.actionsMap.put(index - 1, action);
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setAction")
    public Page action(int index, Action action) {
        return setAction(index, action);
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_page.get_group_index")
    public int getGroupIndex() {
        return this.groupIndex + 1;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "index"
            ),
            value = "wheel_page.set_group_index"
    )
    public Page setGroupIndex(int index) {
        groupIndex = Math.min(Math.max(index - 1, 0), getGroupCount() - 1);
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setGroupIndex")
    public Page groupIndex(int index) {
        return setGroupIndex(index);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "groupIndex"
                    )
            },
            value = "wheel_page.get_group_actions")
    public List<Action> getGroupActions(Integer group) {
        if (group != null && group < 1)
            throw new LuaError("Index must be greater than 0!");
        return Arrays.asList(group(group == null ? groupIndex : group - 1));
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return "keepLastGroup".equals(arg) ? keepLastGroup : null;
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, boolean value) {
        if ("keepLastGroup".equals(key))
            keepLastGroup = value;
        else throw new LuaError("Cannot assign value on key \"" + key + "\"");
    }

    @Override
    public String toString() {
        return title != null ? title + " (Action Wheel Page)" : "Action Wheel Page";
    }
}
