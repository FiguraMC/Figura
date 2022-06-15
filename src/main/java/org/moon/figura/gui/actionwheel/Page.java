package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class Page {

    @LuaWhitelist
    public String name;

    Action[] actions = new Action[8]; //max 8 actions per page

    public Page(String name) {
        this.name = name;
    }

    public int getSize() {
        int count = 0;
        for (Action action : actions)
            if (action != null) count++;
        return Math.max(count, 2);
    }

    @LuaWhitelist
    public static Action addAction(@LuaNotNil Page page, String title, LuaFunction leftAction, LuaFunction rightAction, Object item, Object r, Double g, Double b) {
        //get the first null slot
        int index = -1;
        for (int i = 0; i < page.actions.length; i++) {
            if (page.actions[i] == null) {
                index = i;
                break;
            }
        }

        //if failed to find a null slot, that means the page is full
        if (index == -1)
            throw new LuaRuntimeException("Page " + page.name + " is full!");

        //set the action
        FiguraVec3 color = LuaUtils.parseVec3("addAction", r, g, b);
        ItemStack itemStack = LuaUtils.parseItemStack("addAction", item);
        Action action = new Action(title, color, itemStack, leftAction, rightAction);
        page.actions[index] = action;

        //return the action for convenience
        return action;
    }

    @Override
    public String toString() {
        return "Action Wheel Page (" + name + ")";
    }
}
