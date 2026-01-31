package org.figuramc.figura.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class FiguraGui {
    private static final CachedOrthoProjectionMatrixBuffer guiProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("gui", 1000.0F, 11000.0F, true);

    public static void onRender(GuiGraphics guiGraphics, float tickDelta, CallbackInfo ci) {
        if (AvatarManager.panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        // render popup menu below everything, as if it were in the world
        FiguraMod.pushProfiler("popupMenu");
        PopupMenu.render(guiGraphics);
        FiguraMod.popProfiler();

        // get avatar
        Entity entity = Minecraft.getInstance().getCameraEntity();
        Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);

        Window window = Minecraft.getInstance().getWindow();
        GpuBufferSlice previousProjectionMatrix = RenderSystem.getProjectionMatrixBuffer();
        ProjectionType previousProjectionType = RenderSystem.getProjectionType();

        RenderSystem.setProjectionMatrix(
                guiProjectionMatrixBuffer.getBuffer((float)window.getWidth() / window.getGuiScale(), (float)window.getHeight() / window.getGuiScale()),
                ProjectionType.ORTHOGRAPHIC
        );

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translation(0.0F, 0.0F, -11000.0F);

        if (avatar != null) {
            // hud parent type
            PoseStack stack = new PoseStack();
            stack.pushPose();
            stack.setIdentity();
            stack.last().pose().mul(guiGraphics.pose());

            avatar.hudRender(stack, Minecraft.getInstance().renderBuffers().bufferSource(), entity, tickDelta);
            stack.popPose();
            // hud hidden by script
            if (avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderHUD) {
                // render figura overlays
                renderOverlays(guiGraphics);
                // cancel this method
                ci.cancel();
            }
        }
        matrix4fStack.popMatrix();
        RenderSystem.setProjectionMatrix(previousProjectionMatrix, previousProjectionType);

        FiguraMod.popProfiler();
    }

    public static void renderOverlays(GuiGraphics guiGraphics) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        // render paperdoll
        FiguraMod.pushProfiler("paperdoll");
        PaperDoll.render(guiGraphics, false);

        // render wheel
        FiguraMod.popPushProfiler("actionWheel");
        ActionWheel.render(guiGraphics);

        FiguraMod.popProfiler(2);
    }
}
