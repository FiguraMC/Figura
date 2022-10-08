package org.moon.figura.lua.api.action_wheel;

import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "Action",
        value = "wheel_action"
)
public class Action {

    public static final FiguraVec3 HOVER_COLOR = FiguraVec3.of(1, 1, 1);
    public static final FiguraVec3 TOGGLE_COLOR = FiguraVec3.of(0, 1, 0);

    protected String title, toggleTitle;
    protected ItemStack item, hoverItem, toggleItem;
    protected FiguraVec3 color, hoverColor, toggleColor;
    protected boolean toggled = false;


    // -- function fields -- //


    @LuaWhitelist
    @LuaFieldDoc("wheel_action.left_click")
    public LuaFunction leftClick;
    @LuaWhitelist
    @LuaFieldDoc("wheel_action.right_click")
    public LuaFunction rightClick;

    @LuaWhitelist
    @LuaFieldDoc("wheel_action.toggle")
    public LuaFunction toggle;
    @LuaWhitelist
    @LuaFieldDoc("wheel_action.untoggle")
    public LuaFunction untoggle;

    @LuaWhitelist
    @LuaFieldDoc("wheel_action.scroll")
    public LuaFunction scroll;


    // -- java functions -- //


    public void execute(Avatar avatar, boolean left) {
        //click action
        LuaFunction function = left ? leftClick : rightClick;
        if (function != null)
            avatar.run(function, avatar.tick, this);

        if (!left)
            return;

        //toggle action
        function = toggled ? untoggle == null ? toggle : untoggle : toggle;
        if (function != null) {
            toggled = !toggled;
            avatar.run(function, avatar.tick, toggled, this);
        }
    }

    public void mouseScroll(Avatar avatar, double delta) {
        //scroll action
        if (scroll != null)
            avatar.run(scroll, avatar.tick, delta, this);
    }

    public ItemStack getItem(boolean selected) {
        ItemStack ret = null;
        if (selected)
            ret = hoverItem;
        if (ret == null && toggled)
            ret = toggleItem;
        if (ret == null)
            ret = item;
        return ret;
    }

    public FiguraVec3 getColor(boolean selected) {
        if (selected)
            return hoverColor == null ? HOVER_COLOR : hoverColor;
        else if (toggled)
            return toggleColor == null ? TOGGLE_COLOR : toggleColor;
        else
            return color;
    }


    // -- general functions -- //


    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_title")
    public String getTitle() {
        return toggled ? toggleTitle == null ? title : toggleTitle : title;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
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
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
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
                    @LuaMethodOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
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
                    @LuaMethodOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
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

    
    // -- set functions -- //


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            value = "wheel_action.on_left_click"
    )
    public Action onLeftClick(LuaFunction function) {
        this.leftClick = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            value = "wheel_action.on_right_click"
    )
    public Action onRightClick(LuaFunction function) {
        this.rightClick = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            value = "wheel_action.on_toggle"
    )
    public Action onToggle(LuaFunction function) {
        this.toggle = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            value = "wheel_action.on_untoggle"
    )
    public Action onUntoggle(LuaFunction function) {
        this.untoggle = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "scrollFunction"
            ),
            value = "wheel_action.on_scroll"
    )
    public Action onScroll(LuaFunction function) {
        this.scroll = function;
        return this;
    }


    // -- toggle specific stuff -- //


    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_toggle_title")
    public String getToggleTitle() {
        return this.toggleTitle;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "wheel_action.toggle_title",
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "title"
            )
    )
    public Action toggleTitle(String title) {
        this.toggleTitle = title;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_toggle_color")
    public FiguraVec3 getToggleColor() {
        return this.toggleColor;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "wheel_action.toggle_color"
    )
    public Action toggleColor(Object x, Double y, Double z) {
        this.toggleColor = x == null ? null : LuaUtils.parseVec3("toggleColor", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = ItemStackAPI.class,
                            argumentNames = "item"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "item"
                    )
            },
            value = "wheel_action.toggle_item"
    )
    public Action toggleItem(Object item) {
        this.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("wheel_action.is_toggled")
    public boolean isToggled() {
        return this.toggled;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            value = "wheel_action.toggled"
    )
    public Action toggled(boolean bool) {
        this.toggled = bool;
        return this;
    }


    // -- metamethods -- //


    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "leftClick" -> leftClick;
            case "rightClick" -> rightClick;
            case "toggle" -> toggle;
            case "untoggle" -> untoggle;
            case "scroll" -> scroll;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        if (key == null) return;
        LuaFunction func = value instanceof LuaFunction f ? f : null;
        switch (key) {
            case "leftClick" -> leftClick = func;
            case "rightClick" -> rightClick = func;
            case "toggle" -> toggle = func;
            case "untoggle" -> untoggle = func;
            case "scroll" -> scroll = func;
        }
    }

    @Override
    public String toString() {
        return title == null ? "Action Wheel Action" : "Action Wheel Action (" + title + ")";
    }
}
