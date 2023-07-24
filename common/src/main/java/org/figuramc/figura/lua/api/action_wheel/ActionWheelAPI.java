package org.figuramc.figura.lua.api.action_wheel;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.gui.ActionWheel;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;

import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "ActionWheelAPI",
        value = "action_wheel"
)
public class ActionWheelAPI {

    public Page currentPage;
    private final HashMap<String, Page> pages = new HashMap<>();
    private final boolean isHost;

    @LuaWhitelist
    @LuaFieldDoc("action_wheel.left_click")
    public LuaFunction leftClick;
    @LuaWhitelist
    @LuaFieldDoc("action_wheel.right_click")
    public LuaFunction rightClick;
    @LuaWhitelist
    @LuaFieldDoc("action_wheel.scroll")
    public LuaFunction scroll;

    public ActionWheelAPI(Avatar owner) {
        this.isHost = owner.isHost;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Boolean.class},
                            argumentNames = {"index", "rightClick"}
                    )
            },
            value = "action_wheel.execute"
    )
    public ActionWheelAPI execute(Integer index, boolean right) {
        if (index != null && (index < 1 || index > 8))
            throw new LuaError("index must be between 1 and 8");
        if (this.isHost) ActionWheel.execute(index == null ? ActionWheel.getSelected() : index - 1, !right);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("action_wheel.is_enabled")
    public boolean isEnabled() {
        return this.isHost && ActionWheel.isEnabled();
    }

    @LuaWhitelist
    @LuaMethodDoc("action_wheel.get_selected")
    public int getSelected() {
        return this.isHost ? ActionWheel.getSelected() + 1 : 0;
    }

    @LuaWhitelist
    @LuaMethodDoc("action_wheel.get_selected_action")
    public Action getSelectedAction() {
        if (!this.isHost || this.currentPage == null)
            return null;

        int selected = ActionWheel.getSelected();
        if (selected < 0 || selected > 7)
            return null;

        return this.currentPage.slots()[selected];
    }

    @LuaWhitelist
    @LuaMethodDoc("action_wheel.new_action")
    public Action newAction() {
        return new Action();
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
            value = "action_wheel.new_page"
    )
    public Page newPage(String title) {
        Page page = new Page(title);
        if (title != null) this.pages.put(title, page);
        return page;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "pageTitle"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = Page.class,
                            argumentNames = "page"
                    )
            },
            value = "action_wheel.set_page"
    )
    public ActionWheelAPI setPage(Object page) {
        Page currentPage;
        if (page == null) {
            currentPage = null;
        } else if (page instanceof Page p) {
            currentPage = p;
        } else if (page instanceof String s) {
            currentPage = this.pages.get(s);
            if (currentPage == null) {
                throw new LuaError("Page \"" + s + "\" not found");
            }
        } else {
            throw new LuaError("Invalid page type, expected \"string\" or \"page\"");
        }

        if (currentPage != null && !currentPage.keepSlots)
            currentPage.setSlotsShift(1);

        this.currentPage = currentPage;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            returnType = LuaTable.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "pageTitle",
                            returnType = Page.class
                    )
            },
            value = "action_wheel.get_page"
    )
    public Object getPage(String pageTitle) {
        return pageTitle != null ? this.pages.get(pageTitle) : this.pages;
    }

    @LuaWhitelist
    @LuaMethodDoc("action_wheel.get_current_page")
    public Page getCurrentPage() {
        return this.currentPage;
    }

    public boolean execute(Avatar avatar, boolean left) {
        LuaFunction function = left ? leftClick : rightClick;

        // execute
        if (function != null) {
            Varargs result = avatar.run(function, avatar.tick);
            return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
        }

        return false;
    }

    public boolean mouseScroll(Avatar avatar, double delta) {
        if (scroll != null) {
            Varargs result = avatar.run(scroll, avatar.tick, delta);
            return result != null && result.arg(1).isboolean() && result.arg(1).checkboolean();
        }

        return false;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "leftClick" -> leftClick;
            case "rightClick" -> rightClick;
            case "scroll" -> scroll;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, Object value) {
        LuaFunction val = value instanceof LuaFunction f ? f : null;
        switch (key) {
            case "leftClick" -> leftClick = val;
            case "rightClick" -> rightClick = val;
            case "scroll" -> scroll = val;
            default -> throw new LuaError("Cannot assign value on key \"" + key + "\"");
        }
    }

    @Override
    public String toString() {
        return "ActionWheelAPI";
    }
}
