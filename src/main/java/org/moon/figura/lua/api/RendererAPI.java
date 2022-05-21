package org.moon.figura.lua.api;

import net.minecraft.util.Mth;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
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

    @LuaWhitelist
    @LuaFieldDoc(
            description = "renderer.renderFire"
    )
    public boolean renderFire = true;

    @LuaWhitelist
    @LuaFieldDoc(
            description = "renderer.renderVehicle"
    )
    public boolean renderVehicle = true;

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

    @Override
    public String toString() {
        return "RendererAPI";
    }
}
