package org.figuramc.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPart;
import org.figuramc.figura.model.PartCustomization;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "RenderTask",
        value = "render_task"
)
public abstract class RenderTask {

    protected final String name;
    protected final Avatar owner;
    protected final FiguraModelPart parent;
    protected final PartCustomization customization;

    public RenderTask(String name, Avatar owner, FiguraModelPart parent) {
        this.name = name;
        this.owner = owner;
        this.parent = parent;
        this.customization = new PartCustomization();
        this.customization.visible = true;
    }

    public void render(PartCustomization.PartCustomizationStack stack, MultiBufferSource buffer, int light, int overlay) {
        customization.recalculate();
        stack.push(customization);
        PoseStack poseStack = stack.peek().copyIntoGlobalPoseStack();
        render(poseStack, buffer, light, overlay);
        stack.pop();
    }
    public abstract void render(PoseStack stack, MultiBufferSource buffer, int light, int overlay);
    public abstract int getComplexity();
    public boolean shouldRender() {
        return customization.visible;
    }


    // -- lua stuff -- // 


    @LuaWhitelist
    @LuaMethodDoc("render_task.remove")
    public RenderTask remove() {
        this.parent.removeTask(this);
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_name")
    public String getName() {
        return this.name;
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.is_visible")
    public boolean isVisible() {
        return customization.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            aliases = "visible",
            value = "render_task.set_visible"
    )
    public RenderTask setVisible(boolean visible) {
        customization.visible = visible;
        return this;
    }

    @LuaWhitelist
    public RenderTask visible(boolean visible) {
        return setVisible(visible);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_light")
    public FiguraVec2 getLight() {
        Integer light = customization.light;
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
            aliases = "light",
            value = "render_task.set_light")
    public RenderTask setLight(Object blockLight, Double skyLight) {
        if (blockLight == null) {
            customization.light = null;
            return this;
        }

        FiguraVec2 lightVec = LuaUtils.parseVec2("setLight", blockLight, skyLight);
        customization.light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
        return this;
    }

    @LuaWhitelist
    public RenderTask light(Object blockLight, Double skyLight) {
        return setLight(blockLight, skyLight);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_overlay")
    public FiguraVec2 getOverlay() {
        Integer overlay = customization.overlay;
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
            aliases = "overlay",
            value = "render_task.set_overlay")
    public RenderTask setOverlay(Object whiteOverlay, Double hurtOverlay) {
        if (whiteOverlay == null) {
            customization.overlay = null;
            return this;
        }

        FiguraVec2 overlayVec = LuaUtils.parseVec2("setOverlay", whiteOverlay, hurtOverlay);
        customization.overlay = OverlayTexture.pack((int) overlayVec.x, (int) overlayVec.y);
        return this;
    }

    @LuaWhitelist
    public RenderTask overlay(Object whiteOverlay, Double hurtOverlay) {
        return setOverlay(whiteOverlay, hurtOverlay);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_pos")
    public FiguraVec3 getPos() {
        return this.customization.getPos();
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
            aliases = "pos",
            value = "render_task.set_pos"
    )
    public RenderTask setPos(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setPos", x, y, z);
        this.customization.setPos(vec);
        return this;
    }

    @LuaWhitelist
    public RenderTask pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_rot")
    public FiguraVec3 getRot() {
        return this.customization.getRot();
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
            aliases = "rot",
            value = "render_task.set_rot"
    )
    public RenderTask setRot(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("setRot", x, y, z);
        this.customization.setRot(vec);
        return this;
    }

    @LuaWhitelist
    public RenderTask rot(Object x, Double y, Double z) {
        return setRot(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_scale")
    public FiguraVec3 getScale() {
        return this.customization.getScale();
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
            aliases = "scale",
            value = "render_task.set_scale"
    )
    public RenderTask setScale(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseOneArgVec("setScale", x, y, z, 1d);
        this.customization.setScale(vec);
        return this;
    }

    @LuaWhitelist
    public RenderTask scale(Object x, Double y, Double z) {
        return setScale(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_position_matrix")
    public FiguraMat4 getPositionMatrix() {
        this.customization.recalculate();
        return this.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_position_matrix_raw")
    public FiguraMat4 getPositionMatrixRaw() {
        return this.customization.getPositionMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_normal_matrix")
    public FiguraMat3 getNormalMatrix() {
        this.customization.recalculate();
        return this.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc("render_task.get_normal_matrix_raw")
    public FiguraMat3 getNormalMatrixRaw() {
        return this.customization.getNormalMatrix();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "matrix"
            ),
            aliases = "matrix",
            value = "render_task.set_matrix"
    )
    public RenderTask setMatrix(@LuaNotNil FiguraMat4 matrix) {
        this.customization.setMatrix(matrix);
        return this;
    }

    @LuaWhitelist
    public RenderTask matrix(@LuaNotNil FiguraMat4 mat) {
        return setMatrix(mat);
    }

    @Override
    public String toString() {
        return name + " (Render Task)";
    }
}
