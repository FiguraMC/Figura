package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Action",
        value = "wheel_action"
)
public abstract class Action {

    protected static final FiguraVec3 HOVER_COLOR = FiguraVec3.of(1, 1, 1);

    protected String title;
    protected ItemStack item, hoverItem;
    protected FiguraVec3 color, hoverColor;

    public void execute(Avatar avatar, boolean left) {}

    public void mouseScroll(Avatar avatar, double delta) {}

    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_title")
    public String getTitle() {
        return this.title;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "title"
                    )
            },
            value = "wheel_action.title"
    )
    public Action title(String title) {
        this.title = title;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_color")
    public FiguraVec3 getColor() {
        return this.color;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "wheel_action.color"
    )
    public Action color(Object x, Double y, Double z) {
        this.color = x == null ? null : LuaUtils.parseVec3("color", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_hover_color")
    public FiguraVec3 getHoverColor() {
        return this.hoverColor;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "wheel_action.hover_color"
    )
    public Action hoverColor(Object x, Double y, Double z) {
        this.hoverColor = x == null ? null : LuaUtils.parseVec3("hoverColor", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    )
            },
            value = "wheel_action.item"
    )
    public Action item(Object item) {
        this.item = LuaUtils.parseItemStack("item", item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    )
            },
            value = "wheel_action.hover_item"
    )
    public Action hoverItem(Object item) {
        this.hoverItem = LuaUtils.parseItemStack("hoverItem", item);
        return this;
    }

    public ItemStack getItem(boolean selected) {
        return selected ? hoverItem == null ? item : hoverItem : item;
    }

    public FiguraVec3 getColor(boolean selected) {
        return selected ? hoverColor == null ? HOVER_COLOR : hoverColor : color;
    }

    @Override
    public String toString() {
        return title == null ? "Action Wheel Action" : "Action Wheel Action (" + title + ")";
    }
}
