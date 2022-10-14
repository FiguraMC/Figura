package org.moon.figura.lua.api.action_wheel;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;

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
    public void execute(Integer index, boolean right) {
        if (index != null && (index < 1 || index > 8))
            throw new LuaError("index must be between 1 and 8");
        if (this.isHost) ActionWheel.execute(index == null ? ActionWheel.getSelected() : index - 1, !right);
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
    @LuaMethodDoc("action_wheel.create_action")
    public Action createAction() {
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
            value = "action_wheel.create_page"
    )
    public Page createPage(String title) {
        Page page = new Page();
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
    public void setPage(Object page) {
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

        this.currentPage = currentPage;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "pageTitle"
            ),
            value = "action_wheel.get_page"
    )
    public Page getPage(@LuaNotNil String pageTitle) {
        return this.pages.get(pageTitle);
    }

    @LuaWhitelist
    @LuaMethodDoc("action_wheel.get_current_page")
    public Page getCurrentPage() {
        return this.currentPage;
    }

    public void execute(Avatar avatar, boolean left) {
        LuaFunction function = left ? leftClick : rightClick;

        //execute
        if (function != null)
            avatar.run(function, avatar.tick);
    }

    public void mouseScroll(Avatar avatar, double delta) {
        if (scroll != null)
            avatar.run(scroll, avatar.tick, delta);
    }

    @LuaWhitelist
    public Object __index( String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "leftClick" -> leftClick;
            case "rightClick" -> rightClick;
            case "scroll" -> scroll;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        if (key == null) return;
        LuaFunction val = value instanceof LuaFunction f ? f : null;
        switch (key) {
            case "leftClick" -> leftClick = val;
            case "rightClick" -> rightClick = val;
            case "scroll" -> scroll = val;
        }
    }

    @Override
    public String toString() {
        return "ActionWheelAPI";
    }
}
