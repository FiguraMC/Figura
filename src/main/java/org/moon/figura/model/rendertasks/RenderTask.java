package org.moon.figura.model.rendertasks;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "RenderTask",
        value = "render_task"
)
public abstract class RenderTask {

    protected boolean enabled = true;
    protected Integer light = null;
    protected Integer overlay = null;
    protected final FiguraVec3 pos = FiguraVec3.of();
    protected final FiguraVec3 rot = FiguraVec3.of();
    protected final FiguraVec3 scale = FiguraVec3.of(1, 1, 1);

    //Return true if something was rendered, false if the function cancels for some reason
    public abstract boolean render(PartCustomization.Stack stack, MultiBufferSource buffer, int light, int overlay);
    public abstract int getComplexity();
    private static final PartCustomization dummyCustomization = PartCustomization.of();
    public void pushOntoStack(PartCustomization.Stack stack) {
        dummyCustomization.setScale(scale);
        dummyCustomization.setPos(pos);
        dummyCustomization.setRot(rot);
        dummyCustomization.recalculate();
        stack.push(dummyCustomization);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.is_enabled")
    public boolean isEnabled() {
        return this.enabled;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "bool"
            ),
            value = "render_task.enabled"
    )
    public RenderTask enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_light")
    public FiguraVec2 getLight() {
        return light == null ? null : FiguraVec2.of(LightTexture.block(light), LightTexture.sky(light));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "light"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"blockLight", "skyLight"}
                    )
            },
            value = "render_task.light")
    public RenderTask light(Object blockLight, Double skyLight) {
        if (blockLight == null) {
            light = null;
            return this;
        }

        FiguraVec2 lightVec = LuaUtils.parseVec2("light", blockLight, skyLight);
        light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_overlay")
    public FiguraVec2 getOverlay() {
        return overlay == null ? null : FiguraVec2.of(overlay & 0xFFFF, overlay >> 16);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "overlay"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"whiteOverlay", "hurtOverlay"}
                    )
            },
            value = "render_task.overlay")
    public RenderTask overlay(Object whiteOverlay, Double hurtOverlay) {
        if (whiteOverlay == null) {
            overlay = null;
            return this;
        }

        FiguraVec2 overlayVec = LuaUtils.parseVec2("overlay", whiteOverlay, hurtOverlay);
        overlay = OverlayTexture.pack((int) overlayVec.x, (int) overlayVec.y);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_pos")
    public FiguraVec3 getPos() {
        return this.pos;
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
            value = "render_task.pos"
    )
    public RenderTask pos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("pos", x, y, z);
        pos.set(vec);
        vec.free();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_rot")
    public FiguraVec3 getRot() {
        return this.rot;
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
            value = "render_task.rot"
    )
    public RenderTask rot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("rot", x, y, z);
        rot.set(vec);
        vec.free();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_scale")
    public FiguraVec3 getScale() {
        return this.scale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "render_task.scale"
    )
    public RenderTask scale(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("scale", x, y, z, 1, 1, 1);
        scale.set(vec);
        vec.free();
        return this;
    }

    @Override
    public String toString() {
        return "Render Task";
    }
}
