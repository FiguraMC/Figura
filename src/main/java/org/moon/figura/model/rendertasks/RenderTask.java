package org.moon.figura.model.rendertasks;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaMethodShadow;
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

    protected final String name;

    protected boolean enabled = true;
    protected Integer light = null;
    protected Integer overlay = null;
    protected final FiguraVec3 pos = FiguraVec3.of();
    protected final FiguraVec3 rot = FiguraVec3.of();
    protected final FiguraVec3 scale = FiguraVec3.of(1, 1, 1);

    public RenderTask(String name) {
        this.name = name;
    }

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
            value = "render_task.set_enabled"
    )
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @LuaWhitelist
    @LuaMethodShadow("setEnabled")
    public RenderTask enabled(boolean enabled) {
        setEnabled(enabled);
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
            value = "render_task.set_light")
    public void setLight(Object blockLight, Double skyLight) {
        if (blockLight == null) {
            light = null;
            return;
        }

        FiguraVec2 lightVec = LuaUtils.parseVec2("setLight", blockLight, skyLight);
        light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
    }

    @LuaWhitelist
    @LuaMethodShadow("setLight")
    public RenderTask light(Object blockLight, Double skyLight) {
        setLight(blockLight, skyLight);
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
            value = "render_task.set_overlay")
    public void setOverlay(Object whiteOverlay, Double hurtOverlay) {
        if (whiteOverlay == null) {
            overlay = null;
            return;
        }

        FiguraVec2 overlayVec = LuaUtils.parseVec2("setOverlay", whiteOverlay, hurtOverlay);
        overlay = OverlayTexture.pack((int) overlayVec.x, (int) overlayVec.y);
    }

    @LuaWhitelist
    @LuaMethodShadow("setOverlay")
    public RenderTask overlay(Object whiteOverlay, Double hurtOverlay) {
        setOverlay(whiteOverlay, hurtOverlay);
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
            value = "render_task.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        pos.set(vec);
        vec.free();
    }

    @LuaWhitelist
    @LuaMethodShadow("setPos")
    public RenderTask pos(Object x, Double y, Double z) {
        setPos(x, y, z);
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
            value = "render_task.set_rot"
    )
    public void setRot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setRot", x, y, z);
        rot.set(vec);
        vec.free();
    }

    @LuaWhitelist
    @LuaMethodShadow("setRot")
    public RenderTask rot(Object x, Double y, Double z) {
        setRot(x, y, z);
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
            value = "render_task.set_scale"
    )
    public void setScale(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
        scale.set(vec);
        vec.free();
    }

    @LuaWhitelist
    @LuaMethodShadow("setScale")
    public RenderTask scale(Object x, Double y, Double z) {
        setScale(x, y, z);
        return this;
    }

    @Override
    public String toString() {
        return name + " (Render Task)";
    }
}
