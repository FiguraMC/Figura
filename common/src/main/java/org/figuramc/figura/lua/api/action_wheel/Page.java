package org.figuramc.figura.lua.api.action_wheel;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@LuaWhitelist
@LuaTypeDoc(
        name = "Page",
        value = "wheel_page"
)
public class Page {

    private final String title;

    private final HashMap<Integer, Action> actionsMap = new HashMap<>();

    private int slotsShift = 0;

    @LuaWhitelist
    @LuaFieldDoc("wheel_page.keep_slots")
    public boolean keepSlots = false;

    public Page(String title) {
        this.title = title;
    }

    public int getSize() {
        Action[] actions = slots();
        int i = actions.length;
        while (i > 0 && actions[i - 1] == null) {
            i--;
        }
        return Math.max(i, 2);
    }

    public int getGreatestSlot() {
        int greatest = 0;
        for (Integer i : actionsMap.keySet())
            greatest = Math.max(greatest, i);
        return greatest;
    }

    public int getGroupCount() {
        return getGreatestSlot() / 8 + 1;
    }

    public Action[] slots() {
        return slots(slotsShift);
    }

    public Action[] slots(int shift) {
        Action[] page = new Action[8];
        for (int i = 0; i < 8; i++) {
            page[i] = actionsMap.get(i + 8 * shift);
        }
        return page;
    }

    private int checkIndex(Integer index) {
        // check and fix index
        if (index != null) {
            if (index < 1)
                throw new LuaError("Index must be greater than 0!");

            return index - 1;
        }

        // if no index is given, get the first null slot
        int i = 0;
        while (actionsMap.get(i) != null)
            i++;

        return i;
    }


    // -- lua stuff -- // 


    @LuaWhitelist
    @LuaMethodDoc("wheel_page.should_keep_slots")
    public boolean shouldKeepSlots() {
        return keepSlots;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            value = "wheel_page.set_keep_slots")
    public Page setKeepSlots(boolean bool) {
        keepSlots = bool;
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
        // set the action
        Action action = new Action();
        this.actionsMap.put(checkIndex(index), action);

        // return the action
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
            aliases = "action",
            value = "wheel_page.set_action"
    )
    public Page setAction(Integer index, Action action) {
        if (index == null || index == -1)
            // "why -1 is accepted" you might say
            // because -1 is more elegant for this, as it will return the latest available index
            // same as how lua substring works, but not exactly
            index = this.checkIndex(null) + 1;
        if (index < 1)
            throw new LuaError("Index must be greater than 0!");
        this.actionsMap.put(index - 1, action);
        return this;
    }

    @LuaWhitelist
    public Page action(Integer index, Action action) {
        return setAction(index, action);
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_page.get_slots_shift")
    public int getSlotsShift() {
        return this.slotsShift + 1;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "shift"
            ),
            aliases = "slotsShift",
            value = "wheel_page.set_slots_shift"
    )
    public Page setSlotsShift(int shift) {
        slotsShift = Math.min(Math.max(shift - 1, 0), getGroupCount() - 1);
        return this;
    }

    @LuaWhitelist
    public Page slotsShift(int shift) {
        return setSlotsShift(shift);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "shift"
                    )
            },
            value = "wheel_page.get_actions")
    public Object getActions(Integer shift) {
        if (shift != null) {
            if (shift < 1)
                throw new LuaError("Shift must be greater than 0!");
            return Arrays.asList(slots(shift - 1));
        } else {
            HashMap<Integer, Action> map = new HashMap<>();
            for (Map.Entry<Integer, Action> entry : actionsMap.entrySet())
                map.put(entry.getKey() + 1, entry.getValue());
            return map;
        }
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return "keepSlots".equals(arg) ? keepSlots : null;
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, boolean value) {
        if ("keepSlots".equals(key))
            keepSlots = value;
        else throw new LuaError("Cannot assign value on key \"" + key + "\"");
    }

    @Override
    public String toString() {
        return title != null ? title + " (Action Wheel Page)" : "Action Wheel Page";
    }
}
