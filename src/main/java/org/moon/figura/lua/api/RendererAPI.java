package org.moon.figura.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

import java.util.UUID;

@LuaWhitelist
@LuaTypeDoc(
        name = "RendererAPI",
        value = "renderer"
)
public class RendererAPI {

    private final UUID owner;

    public Float shadowRadius, fov;

    @LuaWhitelist
    @LuaFieldDoc("renderer.render_fire")
    public boolean renderFire = true;
    @LuaWhitelist
    @LuaFieldDoc("renderer.render_vehicle")
    public boolean renderVehicle = true;
    @LuaWhitelist
    @LuaFieldDoc("renderer.render_crosshair")
    public boolean renderCrosshair = true;
    @LuaWhitelist
    @LuaFieldDoc("renderer.force_paperdoll")
    public boolean forcePaperdoll;

    public FiguraVec3 cameraPos;
    public FiguraVec3 cameraPivot;
    public FiguraVec3 cameraOffsetPivot;
    public FiguraVec3 cameraRot;
    public FiguraVec3 cameraOffsetRot;
    public ResourceLocation postShader;

    public RendererAPI(Avatar owner) {
        this.owner = owner.owner;
    }

    private static boolean checkCameraOwner(UUID entity) {
        Entity e = Minecraft.getInstance().getCameraEntity();
        return e != null && e.getUUID().equals(entity);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Float.class,
                            argumentNames = "radius"
                    )
            },
            value = "renderer.set_shadow_radius"
    )
    public void setShadowRadius(Float shadowRadius) {
        this.shadowRadius = shadowRadius == null ? null : Mth.clamp(shadowRadius, 0f, 12f);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_shadow_radius")
    public Float getShadowRadius() {
        return this.shadowRadius;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.is_first_person")
    public boolean isFirstPerson() {
        return checkCameraOwner(this.owner) && Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.is_camera_backwards")
    public boolean isCameraBackwards() {
        return checkCameraOwner(this.owner) && Minecraft.getInstance().options.getCameraType().isMirrored();
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_pos")
    public FiguraVec3 getCameraPos() {
        return this.cameraPos;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "renderer.set_camera_pos"
    )
    public void setCameraPos(Object x, Double y, Double z) {
        this.cameraPos = x == null ? null : LuaUtils.parseVec3("setCameraPos", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_pivot")
    public FiguraVec3 getCameraPivot() {
        return this.cameraPivot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "renderer.set_camera_pivot"
    )
    public void setCameraPivot(Object x, Double y, Double z) {
        this.cameraPivot = x == null ? null : LuaUtils.parseVec3("setCameraPivot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_offset_pivot")
    public FiguraVec3 getCameraOffsetPivot() {
        return this.cameraOffsetPivot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "renderer.offset_camera_pivot"
    )
    public void offsetCameraPivot(Object x, Double y, Double z) {
        this.cameraOffsetPivot = x == null ? null : LuaUtils.parseVec3("offsetCameraPivot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_rot")
    public FiguraVec3 getCameraRot() {
        return this.cameraRot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "renderer.set_camera_rot"
    )
    public void setCameraRot(Object x, Double y, Double z) {
        this.cameraRot = x == null ? null : LuaUtils.parseVec3("setCameraRot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_camera_offset_rot")
    public FiguraVec3 getCameraOffsetRot() {
        return this.cameraOffsetRot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "renderer.offset_camera_rot"
    )
    public void offsetCameraRot(Object x, Double y, Double z) {
        this.cameraOffsetRot = x == null ? null : LuaUtils.parseVec3("offsetCameraRot", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "effect"
            ),
            value = "renderer.set_post_effect"
    )
    public void setPostEffect(String effect) {
        this.postShader = effect == null ? null : new ResourceLocation("shaders/post/" + effect.toLowerCase() + ".json");
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = Float.class,
                            argumentNames = "fov"
                    )
            },
            value = "renderer.set_fov"
    )
    public void setFOV(Float fov) {
        this.fov = fov;
    }

    @LuaWhitelist
    @LuaMethodDoc("renderer.get_fov")
    public Float getFOV() {
        return this.fov;
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "renderFire" -> renderFire;
            case "renderVehicle" -> renderVehicle;
            case "renderCrosshair" -> renderCrosshair;
            case "forcePaperdoll" -> forcePaperdoll;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, boolean value) {
        if (key == null) return;
        switch (key) {
            case "renderFire" -> renderFire = value;
            case "renderVehicle" -> renderVehicle = value;
            case "renderCrosshair" -> renderCrosshair = value;
            case "forcePaperdoll" -> forcePaperdoll = value;
        }
    }

    @Override
    public String toString() {
        return "RendererAPI";
    }
}
