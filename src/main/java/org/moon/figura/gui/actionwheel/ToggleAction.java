package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "ToggleAction",
        value = "toggle_action"
)
public class ToggleAction extends Action {

    protected static final FiguraVec3 TOGGLE_COLOR = FiguraVec3.of(0, 1, 0);

    protected boolean toggled = false;
    protected String toggleTitle;
    protected ItemStack toggleItem;
    protected FiguraVec3 toggleColor;

    @LuaWhitelist
    @LuaFieldDoc("toggle_action.toggle")
    private LuaFunction toggle;
    @LuaWhitelist
    @LuaFieldDoc("toggle_action.untoggle")
    private LuaFunction untoggle;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            value = "toggle_action.on_toggle"
    )
    public Action onToggle(LuaFunction function) {
        this.toggle = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            value = "toggle_action.on_untoggle"
    )
    public Action onUntoggle(LuaFunction function) {
        this.untoggle = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("toggle_action.get_toggle_title")
    public String getToggleTitle() {
        return this.toggleTitle;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "toggle_action.toggle_title",
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "title"
            )
    )
    public Action toggleTitle(String title) {
        this.toggleTitle = title;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("toggle_action.get_toggle_color")
    public FiguraVec3 getToggleColor() {
        return this.toggleColor;
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
            value = "toggle_action.toggle_color"
    )
    public Action toggleColor(Object x, Double y, Double z) {
        this.toggleColor = x == null ? null : LuaUtils.parseVec3("toggleColor", x, y, z);
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
            value = "toggle_action.toggle_item"
    )
    public Action toggleItem(Object item) {
        this.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("toggle_action.is_toggled")
    public boolean isToggled() {
        return this.toggled;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            value = "toggle_action.toggled"
    )
    public Action toggled(boolean bool) {
        this.toggled = bool;
        return this;
    }

    @Override
    public void execute(Avatar avatar, boolean left) {
        if (!left)
            return;

        toggled = !toggled;
        LuaFunction function = !toggled ? untoggle == null ? toggle : untoggle : toggle;

        //execute
        if (function != null)
            avatar.tryCall(function, -1, toggled);
    }

    @Override
    public String getTitle() {
        return toggled ? toggleTitle == null ? title : toggleTitle : title;
    }

    @Override
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

    @Override
    public FiguraVec3 getColor(boolean selected) {
        if (selected)
            return hoverColor == null ? HOVER_COLOR : hoverColor;
        else if (toggled)
            return toggleColor == null ? TOGGLE_COLOR : toggleColor;
        else
            return color;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "toggle" -> toggle;
            case "untoggle" -> untoggle;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        if (key == null) return;
        LuaFunction func = value instanceof LuaFunction f ? f : null;
        switch (key) {
            case "toggle" -> toggle = func;
            case "untoggle" -> untoggle = func;
        }
    }

    @Override
    public String toString() {
        return title == null ? "Action Wheel Toggle" : "Action Wheel Toggle (" + title + ")";
    }
}
