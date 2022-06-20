package org.moon.figura.lua.api;

import org.moon.figura.FiguraMod;
import org.moon.figura.gui.actionwheel.ActionWheel;
import org.moon.figura.gui.actionwheel.Page;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.HashMap;
import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "ActionWheelAPI",
        description = "action_wheel"
)
public class ActionWheelAPI {

    public Page currentPage;
    public HashMap<String, Page> pages = new HashMap<>();
    private final boolean isHost;

    public ActionWheelAPI(UUID owner) {
        this.isHost = FiguraMod.isLocal(owner);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = ActionWheelAPI.class,
                            argumentNames = "api"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ActionWheelAPI.class, Boolean.class},
                            argumentNames = {"api", "rightClick"}
                    )
            },
            description = "action_wheel.execute"
    )
    public static void execute(@LuaNotNil ActionWheelAPI api, Boolean right) {
        if (api.isHost) ActionWheel.execute(right == null || !right);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = ActionWheelAPI.class,
                    argumentNames = "api"
            ),
            description = "action_wheel.is_enabled"
    )
    public static boolean isEnabled(@LuaNotNil ActionWheelAPI api) {
        return api.isHost && ActionWheel.isEnabled();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = ActionWheelAPI.class,
                            argumentNames = "api"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ActionWheelAPI.class, String.class},
                            argumentNames = {"api", "title"}
                    )
            },
            description = "action_wheel.create_page"
    )
    public static Page createPage(@LuaNotNil ActionWheelAPI api, String title) {
        Page page = new Page();
        if (title != null) api.pages.put(title, page);
        return page;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {ActionWheelAPI.class, String.class},
                            argumentNames = {"api", "pageTitle"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {ActionWheelAPI.class, Page.class},
                            argumentNames = {"api", "page"}
                    )
            },
            description = "action_wheel.set_page"
    )
    public static void setPage(@LuaNotNil ActionWheelAPI api, Object page) {
        Page currentPage;
        if (page == null) {
            currentPage = null;
        } else if (page instanceof Page p) {
            currentPage = p;
        } else if (page instanceof String s) {
            currentPage = api.pages.get(s);
            if (currentPage == null) {
                throw new LuaRuntimeException("Page \"" + s + "\" not found");
            }
        } else {
            throw new LuaRuntimeException("Invalid page type, expected \"string\" or \"page\"");
        }

        if (api.isHost) api.currentPage = currentPage;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {ActionWheelAPI.class, String.class},
                    argumentNames = {"api", "pageTitle"}
            ),
            description = "action_wheel.get_page"
    )
    public static Page getPage(@LuaNotNil ActionWheelAPI api, @LuaNotNil String pageTitle) {
        return api.pages.get(pageTitle);
    }

    @Override
    public String toString() {
        return "ActionWheelAPI";
    }
}
