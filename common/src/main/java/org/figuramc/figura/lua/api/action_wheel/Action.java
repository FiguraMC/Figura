package org.figuramc.figura.lua.api.action_wheel;

import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;

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
    @LuaFieldDoc("wheel_action.middle_click")
    public LuaFunction middleClick;
    @LuaWhitelist
    @LuaFieldDoc("wheel_action.click")
    public LuaFunction click;

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


    public boolean mouseClicked(Avatar avatar, int button) {
        // click action
        LuaFunction function = switch (button) {
            case 0 -> leftClick;
            case 1 -> rightClick;
            case 2 -> middleClick;
            default -> null;
        };
        boolean next = true;
        if (function != null) {
            Varargs result = avatar.run(function, avatar.tick, this);
            next = !(result != null && result.arg(1).isboolean() && result.arg(1).checkboolean());
        }

        if (next && click != null) {
            Varargs result = avatar.run(click, avatar.tick, button, this);
            next = !(result != null && result.arg(1).isboolean() && result.arg(1).checkboolean());
        }

        if (button != 0)
            return !next;

        // toggle action
        function = toggled ? untoggle == null ? toggle : untoggle : toggle;
        if (function != null) {
            toggled = !toggled;
            avatar.run(function, avatar.tick, toggled, this);
            return true;
        }
        return false;
    }

    public boolean mouseScroll(Avatar avatar, double delta) {
        // scroll action
        if (scroll != null) {
            Varargs result = avatar.run(scroll, avatar.tick, delta, this);
            return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
        }
        return false;
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
            aliases = "title",
            value = "wheel_action.set_title"
    )
    public Action setTitle(String title) {
        this.title = title;
        return this;
    }

    @LuaWhitelist
    public Action title(String title) {
        return setTitle(title);
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
            aliases = "color",
            value = "wheel_action.set_color"
    )
    public Action setColor(Object x, Double y, Double z) {
        this.color = LuaUtils.nullableVec3("color", x, y, z);
        return this;
    }

    @LuaWhitelist
    public Action color(Object x, Double y, Double z) {
        return setColor(x, y, z);
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
            aliases = "hoverColor",
            value = "wheel_action.set_hover_color"
    )
    public Action setHoverColor(Object x, Double y, Double z) {
        this.hoverColor = LuaUtils.nullableVec3("hoverColor", x, y, z);
        return this;
    }

    @LuaWhitelist
    public Action hoverColor(Object x, Double y, Double z) {
        return setHoverColor(x, y, z);
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
            aliases = "item",
            value = "wheel_action.set_item"
    )
    public Action setItem(Object item) {
        this.item = LuaUtils.parseItemStack("item", item);
        return this;
    }

    @LuaWhitelist
    public Action item(Object item) {
        return setItem(item);
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
            aliases = "hoverItem",
            value = "wheel_action.set_hover_item"
    )
    public Action setHoverItem(Object item) {
        this.hoverItem = LuaUtils.parseItemStack("hoverItem", item);
        return this;
    }

    @LuaWhitelist
    public Action hoverItem(Object item) {
        return setHoverItem(item);
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
            aliases = "texture",
            value = "wheel_action.set_texture"
    )
    public Action setTexture(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        this.texture = texture == null ? null : new TextureData(texture, u, v, width, height, scale);
        return this;
    }

    @LuaWhitelist
    public Action texture(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        return setTexture(texture, u, v, width, height, scale);
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
            aliases = "hoverTexture",
            value = "wheel_action.set_hover_texture"
    )
    public Action setHoverTexture(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        this.hoverTexture = texture == null ? null : new TextureData(texture, u, v, width, height, scale);
        return this;
    }

    @LuaWhitelist
    public Action hoverTexture(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        return setHoverTexture(texture, u, v, width, height, scale);
    }


    // -- set functions -- // 


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            aliases = "onLeftClick",
            value = "wheel_action.set_on_left_click"
    )
    public Action setOnLeftClick(LuaFunction function) {
        this.leftClick = function;
        return this;
    }

    @LuaWhitelist
    public Action onLeftClick(LuaFunction function) {
        return setOnLeftClick(function);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            aliases = "onRightClick",
            value = "wheel_action.set_on_right_click"
    )
    public Action setOnRightClick(LuaFunction function) {
        this.rightClick = function;
        return this;
    }

    @LuaWhitelist
    public Action onRightClick(LuaFunction function) {
        return setOnRightClick(function);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            aliases = "onMiddleClick",
            value = "wheel_action.set_on_middle_click"
    )
    public Action setOnMiddleClick(LuaFunction function) {
        this.rightClick = function;
        return this;
    }

    @LuaWhitelist
    public Action onMiddleClick(LuaFunction function) {
        return setOnMiddleClick(function);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            aliases = "onClick",
            value = "wheel_action.set_on_click"
    )
    public Action setOnClick(LuaFunction function) {
        this.rightClick = function;
        return this;
    }

    @LuaWhitelist
    public Action onClick(LuaFunction function) {
        return setOnClick(function);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "leftFunction"
            ),
            aliases = "onToggle",
            value = "wheel_action.set_on_toggle"
    )
    public Action setOnToggle(LuaFunction function) {
        this.toggle = function;
        return this;
    }

    @LuaWhitelist
    public Action onToggle(LuaFunction function) {
        return setOnToggle(function);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "rightFunction"
            ),
            aliases = "onUntoggle",
            value = "wheel_action.set_on_untoggle"
    )
    public Action setOnUntoggle(LuaFunction function) {
        this.untoggle = function;
        return this;
    }

    @LuaWhitelist
    public Action onUntoggle(LuaFunction function) {
        return setOnUntoggle(function);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = LuaFunction.class,
                    argumentNames = "scrollFunction"
            ),
            aliases = "onScroll",
            value = "wheel_action.set_on_scroll"
    )
    public Action setOnScroll(LuaFunction function) {
        this.scroll = function;
        return this;
    }

    @LuaWhitelist
    public Action onScroll(LuaFunction function) {
        return setOnScroll(function);
    }


    // -- toggle specific stuff -- // 


    @LuaWhitelist
    @LuaMethodDoc("wheel_action.get_toggle_title")
    public String getToggleTitle() {
        return this.toggleTitle;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "title"
            ),
            aliases = "toggleTitle",
            value = "wheel_action.set_toggle_title"
    )
    public Action setToggleTitle(String title) {
        this.toggleTitle = title;
        return this;
    }

    @LuaWhitelist
    public Action toggleTitle(String title) {
        return setToggleTitle(title);
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
            aliases = "toggleColor",
            value = "wheel_action.set_toggle_color"
    )
    public Action setToggleColor(Object x, Double y, Double z) {
        this.toggleColor = LuaUtils.nullableVec3("toggleColor", x, y, z);
        return this;
    }

    @LuaWhitelist
    public Action toggleColor(Object x, Double y, Double z) {
        return setToggleColor(x, y, z);
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
            aliases = "toggleItem",
            value = "wheel_action.set_toggle_item"
    )
    public Action setToggleItem(Object item) {
        this.toggleItem = LuaUtils.parseItemStack("toggleItem", item);
        return this;
    }

    @LuaWhitelist
    public Action toggleItem(Object item) {
        return setToggleItem(item);
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
            aliases = "toggleTexture",
            value = "wheel_action.set_toggle_texture"
    )
    public Action setToggleTexture(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        this.toggleTexture = texture == null ? null : new TextureData(texture, u, v, width, height, scale);
        return this;
    }

    @LuaWhitelist
    public Action toggleTexture(FiguraTexture texture, double u, double v, Integer width, Integer height, Double scale) {
        return setToggleTexture(texture, u, v, width, height, scale);
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
            aliases = "toggled",
            value = "wheel_action.set_toggled"
    )
    public Action setToggled(boolean bool) {
        this.toggled = bool;
        return this;
    }

    @LuaWhitelist
    public Action toggled(boolean bool) {
        return setToggled(bool);
    }


    // -- metamethods -- // 


    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "leftClick" -> leftClick;
            case "rightClick" -> rightClick;
            case "middleClick" -> middleClick;
            case "click" -> click;
            case "toggle" -> toggle;
            case "untoggle" -> untoggle;
            case "scroll" -> scroll;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, Object value) {
        LuaFunction fun = value instanceof LuaFunction f ? f : null;
        switch (key) {
            case "leftClick" -> leftClick = fun;
            case "rightClick" -> rightClick = fun;
            case "middleClick" -> middleClick = fun;
            case "click" -> click = fun;
            case "toggle" -> toggle = fun;
            case "untoggle" -> untoggle = fun;
            case "scroll" -> scroll = fun;
            default -> throw new LuaError("Cannot assign value on key \"" + key + "\"");
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
