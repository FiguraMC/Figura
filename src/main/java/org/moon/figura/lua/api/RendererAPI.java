package org.moon.figura.lua.api;

import net.minecraft.util.Mth;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "RendererAPI",
        description = "renderer"
)
public class RendererAPI {

    public Float shadowRadius;
    public Boolean renderFireOverlay;

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {RendererAPI.class, Float.class},
                    argumentNames = {"api", "radius"}
            ),
            description = "renderer.set_shadow_radius"
    )
    public static void setShadowRadius(@LuaNotNil RendererAPI api, Float shadowRadius) {
        api.shadowRadius = shadowRadius == null ? null : Mth.clamp(shadowRadius, 0f, 12f);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = RendererAPI.class,
                    argumentNames = "api"
            ),
            description = "renderer.get_shadow_radius"
    )
    public static Float getShadowRadius(@LuaNotNil RendererAPI api) {
        return api.shadowRadius;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {RendererAPI.class, Boolean.class},
                    argumentNames = {"api", "bool"}
            ),
            description = "renderer.set_render_fire_overlay"
    )
    public static void setRenderFireOverlay(@LuaNotNil RendererAPI api, Boolean bool) {
        api.renderFireOverlay = bool;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = RendererAPI.class,
                    argumentNames = "api"
            ),
            description = "renderer.can_render_fire_overlay"
    )
    public static Boolean canRenderFireOverlay(@LuaNotNil RendererAPI api) {
        return api.renderFireOverlay;
    }

    @Override
    public String toString() {
        return "RendererAPI";
    }
}
