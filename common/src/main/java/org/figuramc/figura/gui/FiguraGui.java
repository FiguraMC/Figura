package org.figuramc.figura.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.figuramc.figura.mixin.gui.GuiGraphicsAccessor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FiguraGui {

    public static void onRender(GuiGraphicsExtractor guiGraphics, float tickDelta, CallbackInfo ci) {
        if (AvatarManager.panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        // render popup menu below everything, as if it were in the world
        FiguraMod.pushProfiler("popupMenu");
        PopupMenu.extractRenderState(guiGraphics);
        FiguraMod.popProfiler();

        // get avatar
        Entity entity = Minecraft.getInstance().getCameraEntity();
        Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);

        if (avatar != null) {
            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();
            ((GuiGraphicsAccessor) guiGraphics).figura$getRenderState().addPicturesInPictureState(
                    new HudRenderState(avatar, entity, tickDelta, new Matrix3x2f(guiGraphics.pose()), width, height,
                            ((GuiGraphicsAccessor) guiGraphics).figura$getScissorStack().peek()));
            // hud hidden by script
            if (avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderHUD) {
                // render figura overlays
                renderOverlays(guiGraphics);
                // cancel this method
                ci.cancel();
            }
        }


        FiguraMod.popProfiler();
    }

    public static void renderOverlays(GuiGraphicsExtractor guiGraphics) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        // render paperdoll
        FiguraMod.pushProfiler("paperdoll");
        PaperDoll.extractRenderState(guiGraphics, false);

        // render wheel
        FiguraMod.popPushProfiler("actionWheel");
        ActionWheel.extractRenderState(guiGraphics);

        FiguraMod.popProfiler(2);
    }

    public record HudRenderState(Avatar avatar, Entity entity, float tickDelta, Matrix3x2fc guiPose,
                                 int x1, int y1, ScreenRectangle scissorArea) implements PictureInPictureRenderState {
        @Override public int x0() { return 0; }
        @Override public int y0() { return 0; }
        @Override public float scale() { return 1; }
        @Override public ScreenRectangle bounds() {
            return PictureInPictureRenderState.getBounds(0, 0, x1, y1, scissorArea);
        }
    }

    public static class HudRenderer extends PictureInPictureRenderer<HudRenderState> {
        private final Projection projection = new Projection();
        private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("Figura HUD");

        @Override public Class<HudRenderState> getRenderStateClass() { return HudRenderState.class; }
        @Override protected String getTextureLabel() { return "figura-hud"; }

        @Override
        public void prepare(HudRenderState state, GuiRenderState guiState, FeatureRenderDispatcher dispatcher, int scale) {
            GpuBufferSlice previousLights = RenderSystem.getShaderLights();
            try {
                super.prepare(state, guiState, dispatcher, scale);
            } finally {
                RenderSystem.setShaderLights(previousLights);
            }
        }
        @Override
        protected void renderToTexture(HudRenderState state, PoseStack stack, SubmitNodeCollector collector) {
            float guiScale = stack.last().pose().m00();
            projection.setupOrtho(1000.0F, 11000.0F, state.x1() * guiScale, state.y1() * guiScale, true);
            RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
            stack.translate(-state.x1() / 2.0F, -state.y1(), 0);
            stack.scale(1, 1, -1 / guiScale);
            stack.translate(0, 0, -11000);
            stack.last().pose().mul(state.guiPose());
            state.avatar().hudRender(stack, collector, state.entity(), state.tickDelta());
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        }

        @Override public void close() {
            super.close();
            projectionBuffer.close();
        }
    }
}
