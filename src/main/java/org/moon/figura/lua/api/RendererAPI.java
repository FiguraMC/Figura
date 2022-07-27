package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "RendererAPI",
        description = "renderer"
)
public class RendererAPI {

    private final UUID owner;
    public Float shadowRadius;

    @LuaWhitelist
    @LuaFieldDoc(
            description = "renderer.render_fire"
    )
    public boolean renderFire = true;

    @LuaWhitelist
    @LuaFieldDoc(
            description = "renderer.render_vehicle"
    )
    public boolean renderVehicle = true;

    public FiguraVec3 cameraRot;
    public FiguraVec3 cameraBonusRot;
    public ResourceLocation postShader;

    public RendererAPI(UUID owner) {
        this.owner = owner;
    }

    private static boolean checkCameraOwner(UUID entity) {
        Entity e = Minecraft.getInstance().getCameraEntity();
        return e != null && e.getUUID().compareTo(entity) == 0;
    }

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
                    argumentTypes = RendererAPI.class,
                    argumentNames = "api"
            ),
            description = "renderer.is_first_person"
    )
    public static Boolean isFirstPerson(@LuaNotNil RendererAPI api) {
        return checkCameraOwner(api.owner) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = RendererAPI.class,
                    argumentNames = "api"
            ),
            description = "renderer.is_camera_backwards"
    )
    public static Boolean isCameraBackwards(@LuaNotNil RendererAPI api) {
        return checkCameraOwner(api.owner) && Minecraft.getInstance().options.getCameraType().isMirrored();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {RendererAPI.class, FiguraVec3.class},
                            argumentNames = {"api", "rot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {RendererAPI.class, Double.class, Double.class, Double.class},
                            argumentNames = {"api", "x", "y", "z"}
                    )
            },
            description = "renderer.set_camera_rot"
    )
    public static void setCameraRot(@LuaNotNil RendererAPI api, Object x, Double y, Double z) {
        api.cameraRot = x == null ? null : LuaUtils.oldParseVec3("setCameraRot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {RendererAPI.class, FiguraVec3.class},
                            argumentNames = {"api", "rot"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {RendererAPI.class, Double.class, Double.class, Double.class},
                            argumentNames = {"api", "x", "y", "z"}
                    )
            },
            description = "renderer.offset_camera_rot"
    )
    public static void offsetCameraRot(@LuaNotNil RendererAPI api, Object x, Double y, Double z) {
        api.cameraBonusRot = x == null ? null : LuaUtils.oldParseVec3("offsetCameraRot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {RendererAPI.class, String.class},
                    argumentNames = {"api", "effect"}
            ),
            description = "renderer.set_post_effect"
    )
    public static void setPostEffect(@LuaNotNil RendererAPI api, String effect) {
        api.postShader = effect == null ? null : new ResourceLocation("shaders/post/" + effect.toLowerCase() + ".json");
    }

    @Override
    public String toString() {
        return "RendererAPI";
    }
}
