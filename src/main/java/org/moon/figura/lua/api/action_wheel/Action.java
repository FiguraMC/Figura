package org.moon.figura.lua.api.action_wheel;

import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.docs.*;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.rendering.texture.FiguraTexture;
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
    protected TextureData texture, hoverTexture, toggleTexture;
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

    public TextureData getTexture(boolean selected) {
        TextureData ret = null;
        if (selected)
            ret = hoverTexture;
        if (ret == null && toggled)
            ret = toggleTexture;
        if (ret == null)
            ret = texture;
        return ret;
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
            value = "wheel_action.set_title"
    )
    public void setTitle(String title) {
        this.title = title;
    }

    @LuaWhitelist
    @LuaMethodShadow("setTitle")
    public Action title(String title) {
        setTitle(title);
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
            value = "wheel_action.set_color"
    )
    public void setColor(Object x, Double y, Double z) {
        this.color = x == null ? null : LuaUtils.parseVec3("color", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodShadow("setColor")
    public Action color(Object x, Double y, Double z) {
        setColor(x, y, z);
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
            value = "wheel_action.set_hover_color"
    )
    public void setHoverColor(Object x, Double y, Double z) {
        this.hoverColor = x == null ? null : LuaUtils.parseVec3("hoverColor", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodShadow("setHoverColor")
    public Action hoverColor(Object x, Double y, Double z) {
        setHoverColor(x, y, z);
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
            value = "wheel_action.set_item"
    )
    public void setItem(Object item) {
        this.item = LuaUtils.parseItemStack("item", item);
    }

    @LuaWhitelist
    @LuaMethodShadow("setItem")
    public Action item(Object item) {
        setItem(item);
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
            value = "wheel_action.set_hover_item"
    )
    public void setHoverItem(Object item) {
        this.hoverItem = LuaUtils.parseItemStack("hoverItem", item);
    }

    @LuaWhitelist
    @LuaMethodShadow("setHoverItem")
    public Action hoverItem(Object item) {
        setHoverItem(item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraTexture.class,
                            argumentNames = "texture"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class},
                            argumentNames = {"texture", "u", "v"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class},
                            argumentNames = {"texture", "u", "v", "width", "height"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class, Double.class},
                            argumentNames = {"texture", "u", "v", "width", "height", "scale"}
                    )
            },
            value = "wheel_action.set_texture"
    )
    public void setTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        this.texture = new TextureData(texture, u, v, width, height, scale);
    }

    @LuaWhitelist
    @LuaMethodShadow("setTexture")
    public Action texture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        setTexture(texture, u, v, width, height, scale);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraTexture.class,
                            argumentNames = "texture"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class},
                            argumentNames = {"texture", "u", "v"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class},
                            argumentNames = {"texture", "u", "v", "width", "height"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class, Double.class},
                            argumentNames = {"texture", "u", "v", "width", "height", "scale"}
                    )
            },
            value = "wheel_action.set_hover_texture"
    )
    public void setHoverTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        this.hoverTexture = new TextureData(texture, u, v, width, height, scale);
    }

    @LuaWhitelist
    @LuaMethodShadow("setHoverTexture")
    public Action hoverTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        setHoverTexture(texture, u, v, width, height, scale);
        return this;
    }


    // -- set functions -- //


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            value = "wheel_action.set_on_left_click"
    )
    public void setOnLeftClick(LuaFunction function) {
        this.leftClick = function;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOnLeftClick")
    public Action onLeftClick(LuaFunction function) {
        setOnLeftClick(function);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            value = "wheel_action.set_on_right_click"
    )
    public void setOnRightClick(LuaFunction function) {
        this.rightClick = function;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOnRightClick")
    public Action onRightClick(LuaFunction function) {
        setOnRightClick(function);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            value = "wheel_action.set_on_toggle"
    )
    public void setOnToggle(LuaFunction function) {
        this.toggle = function;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOnToggle")
    public Action onToggle(LuaFunction function) {
        setOnToggle(function);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            value = "wheel_action.set_on_untoggle"
    )
    public void setOnUntoggle(LuaFunction function) {
        this.untoggle = function;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOnUntoggle")
    public Action onUntoggle(LuaFunction function) {
        setOnUntoggle(function);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "scrollFunction"
            ),
            value = "wheel_action.set_on_scroll"
    )
    public void setOnScroll(LuaFunction function) {
        this.scroll = function;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOnScroll")
    public Action onScroll(LuaFunction function) {
        setOnScroll(function);
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
            value = "wheel_action.set_toggle_title",
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "title"
            )
    )
    public void setToggleTitle(String title) {
        this.toggleTitle = title;
    }

    @LuaWhitelist
    @LuaMethodShadow("setToggleTitle")
    public Action toggleTitle(String title) {
        setToggleTitle(title);
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
            value = "wheel_action.set_toggle_color"
    )
    public void setToggleColor(Object x, Double y, Double z) {
        this.toggleColor = x == null ? null : LuaUtils.parseVec3("toggleColor", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodShadow("setToggleColor")
    public Action toggleColor(Object x, Double y, Double z) {
        setToggleColor(x, y, z);
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
            value = "wheel_action.set_toggle_item"
    )
    public void setToggleItem(Object item) {
        this.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
    }

    @LuaWhitelist
    @LuaMethodShadow("setToggleItem")
    public Action toggleItem(Object item) {
        setToggleItem(item);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraTexture.class,
                            argumentNames = "texture"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class},
                            argumentNames = {"texture", "u", "v"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class},
                            argumentNames = {"texture", "u", "v", "width", "height"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Double.class, Double.class, Integer.class, Integer.class, Double.class},
                            argumentNames = {"texture", "u", "v", "width", "height", "scale"}
                    )
            },
            value = "wheel_action.set_toggle_texture"
    )
    public void setToggleTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        this.toggleTexture = new TextureData(texture, u, v, width, height, scale);
    }

    @LuaWhitelist
    @LuaMethodShadow("setToggleTexture")
    public Action toggleTexture(@LuaNotNil FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        setToggleTexture(texture, u, v, width, height, scale);
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
            value = "wheel_action.set_toggled"
    )
    public void setToggled(boolean bool) {
        this.toggled = bool;
    }

    @LuaWhitelist
    @LuaMethodShadow("setToggled")
    public Action toggled(boolean bool) {
        setToggled(bool);
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

    public static class TextureData {

        public final FiguraTexture texture;
        public final double u, v, scale;
        public final int width, height;

        public TextureData(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
            this.texture = texture;
            this.u = u;
            this.v = v;
            this.width = width == null ? texture.getWidth() : width;
            this.height = height == null ? texture.getHeight() : height;
            this.scale = scale == null ? 1d : scale;
        }
    }
}
