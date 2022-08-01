package org.moon.figura.gui.actionwheel;

import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.LuaUtils;

@LuaType(typeName = "toggle_action")
@LuaTypeDoc(
        name = "Toggle Action",
        description = "toggle_action"
)
public class ToggleAction extends Action {

    protected static final FiguraVec3 TOGGLE_COLOR = FiguraVec3.of(0, 1, 0);

    protected boolean toggled = false;
    protected String toggleTitle;
    protected ItemStack toggleItem;
    protected FiguraVec3 toggleColor;

    @LuaFieldDoc(description = "toggle_action.toggle")
    private LuaFunction toggle;
    @LuaFieldDoc(description = "toggle_action.untoggle")
    private LuaFunction untoggle;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            description = "toggle_action.on_toggle"
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
            description = "toggle_action.on_untoggle"
    )
    public Action onUntoggle(LuaFunction function) {
        this.untoggle = function;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            description = "toggle_action.toggle_title",
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
            description = "toggle_action.toggle_color"
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
            description = "toggle_action.toggle_item"
    )
    public Action toggleItem(Object item) {
        this.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            description = "toggle_action.toggled"
    )
    public Action toggled(@LuaNotNil Boolean bool) {
        this.toggled = bool;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "toggle_action.is_toggled")
    public boolean isToggled() {
        return this.toggled;
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
        return switch (arg) {
            case "toggle" -> toggle;
            case "untoggle" -> untoggle;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        switch (key) {
            case "toggle" -> toggle = (LuaFunction) value;
            case "untoggle" -> untoggle = (LuaFunction) value;
        }
    }

    @Override
    public String toString() {
        return "Action Wheel Toggle (" + title + ")";
    }
}
