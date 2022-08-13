package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
    @LuaFieldDoc(description = "renderer.render_fire")
    public boolean renderFire = true;
    @LuaWhitelist
    @LuaFieldDoc(description = "renderer.render_vehicle")
    public boolean renderVehicle = true;

    public FiguraVec3 cameraPos;
    public FiguraVec3 cameraPivot;
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
            overloads = {
                    @LuaFunctionOverload,
                    @LuaFunctionOverload(
                            argumentTypes = Float.class,
                            argumentNames = "radius"
                    )
            },
            description = "renderer.set_shadow_radius"
    )
    public void setShadowRadius(Float shadowRadius) {
        this.shadowRadius = shadowRadius == null ? null : Mth.clamp(shadowRadius, 0f, 12f);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "renderer.get_shadow_radius")
    public Float getShadowRadius() {
        return this.shadowRadius;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "renderer.is_first_person")
    public boolean isFirstPerson() {
        return checkCameraOwner(this.owner) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "renderer.is_camera_backwards")
    public boolean isCameraBackwards() {
        return checkCameraOwner(this.owner) && Minecraft.getInstance().options.getCameraType().isMirrored();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "renderer.set_camera_pos"
    )
    public void setCameraPos(Object x, Double y, Double z) {
        this.cameraPos = x == null ? null : LuaUtils.parseVec3("setCameraPos", x, y, z);
    }

    //@LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "renderer.set_camera_pivot"
    )
    public void setCameraPivot(Object x, Double y, Double z) {
        this.cameraPivot = x == null ? null : LuaUtils.parseVec3("setCameraPivot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "renderer.set_camera_rot"
    )
    public void setCameraRot(Object x, Double y, Double z) {
        this.cameraRot = x == null ? null : LuaUtils.parseVec3("setCameraRot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "renderer.offset_camera_rot"
    )
    public void offsetCameraRot(Object x, Double y, Double z) {
        this.cameraBonusRot = x == null ? null : LuaUtils.parseVec3("offsetCameraRot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "effect"
            ),
            description = "renderer.set_post_effect"
    )
    public void setPostEffect(String effect) {
        this.postShader = effect == null ? null : new ResourceLocation("shaders/post/" + effect.toLowerCase() + ".json");
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "renderFire" -> renderFire;
            case "renderVehicle" -> renderVehicle;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, boolean value) {
        if (key == null) return;
        switch (key) {
            case "renderFire" -> renderFire = value;
            case "renderVehicle" -> renderVehicle = value;
        }
    }

    @Override
    public String toString() {
        return "RendererAPI";
    }
}
