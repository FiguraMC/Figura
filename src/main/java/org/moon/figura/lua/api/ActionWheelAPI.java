package org.moon.figura.lua.api;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.gui.actionwheel.ActionWheel;
import org.moon.figura.gui.actionwheel.Page;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.HashMap;
import java.util.UUID;

@LuaType(typeName = "action_wheel")
@LuaTypeDoc(
        name = "ActionWheelAPI",
        description = "action_wheel"
)
public class ActionWheelAPI {

    public Page currentPage;
    private final HashMap<String, Page> pages = new HashMap<>();
    private final boolean isHost;

    @LuaFieldDoc(description = "action_wheel.left_click")
    public LuaFunction leftClick;
    @LuaFieldDoc(description = "action_wheel.right_click")
    public LuaFunction rightClick;
    @LuaFieldDoc(description = "action_wheel.scroll")
    public LuaFunction scroll;

    public ActionWheelAPI(UUID owner) {
        this.isHost = FiguraMod.isLocal(owner);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Integer.class,
                            argumentNames = "index"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Integer.class, Boolean.class},
                            argumentNames = {"index", "rightClick"}
                    )
            },
            description = "action_wheel.execute"
    )
    public void execute(Integer index, Boolean right) {
        if (index != null && (index < 1 || index > 8))
            throw new LuaError("index must be between 1 and 8");
        if (this.isHost) ActionWheel.execute(index == null ? ActionWheel.getSelected() : index - 1, right == null || !right);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "action_wheel.is_enabled")
    public boolean isEnabled() {
        return this.isHost && ActionWheel.isEnabled();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "action_wheel.get_selected")
    public int getSelected() {
        return this.isHost ? ActionWheel.getSelected() + 1 : 0;
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
            description = "action_wheel.create_page"
    )
    public Page createPage(String title) {
        Page page = new Page();
        if (title != null) this.pages.put(title, page);
        return page;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = String.class,
                            argumentNames = "pageTitle"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = Page.class,
                            argumentNames = "page"
                    )
            },
            description = "action_wheel.set_page"
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
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "pageTitle"
            ),
            description = "action_wheel.get_page"
    )
    public Page getPage(@LuaNotNil String pageTitle) {
        return this.pages.get(pageTitle);
    }

    public void execute(Avatar avatar, boolean left) {
        LuaFunction function = left ? leftClick : rightClick;

        //execute
        if (function != null)
            avatar.tryCall(function, -1, null);
    }

    public void mouseScroll(Avatar avatar, double delta) {
        if (scroll != null)
            avatar.tryCall(scroll, -1, delta);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return switch (arg) {
            case "leftClick" -> leftClick;
            case "rightClick" -> rightClick;
            case "scroll" -> scroll;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, Object value) {
        switch (key) {
            case "leftClick" -> leftClick = (LuaFunction) value;
            case "rightClick" -> rightClick = (LuaFunction) value;
            case "scroll" -> scroll = (LuaFunction) value;
        }
    }

    @Override
    public String toString() {
        return "ActionWheelAPI";
    }
}
