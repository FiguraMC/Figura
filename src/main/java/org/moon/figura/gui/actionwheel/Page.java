package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackWrapper;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaFunction;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "Page",
        description = "wheel_page"
)
public class Page {

    @LuaWhitelist
    @LuaFieldDoc(description = "wheel_page.name")
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
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = Page.class,
                            argumentNames = "page"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class},
                            argumentNames = {"page", "title"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class},
                            argumentNames = {"page", "title", "leftAction"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class, String.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction", "item"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class, ItemStackWrapper.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction", "item"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class, String.class, FiguraVec3.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction", "item", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class, ItemStackWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction", "item", "r", "g", "b"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class, String.class, FiguraVec3.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction", "item", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Page.class, String.class, LuaFunction.class, LuaFunction.class, ItemStackWrapper.class, Double.class, Double.class, Double.class},
                            argumentNames = {"page", "title", "leftAction", "rightAction", "item", "r", "g", "b"}
                    )
            },
            description = "wheel_page.add_action"
    )
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
        return "Action Wheel Page (" + name + ")";
    }
}
