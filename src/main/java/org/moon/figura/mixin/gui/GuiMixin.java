package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.gui.PaperDoll;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.lua.api.RendererAPI;
import org.moon.figura.math.vector.FiguraVec2;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;
    @Unique private FiguraVec2 crosshairOffset;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void onRender(PoseStack stack, float tickDelta, CallbackInfo ci) {
        if (AvatarManager.panic)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        //render popup menu below everything, as if it were in the world
        FiguraMod.pushProfiler("popupMenu");
        PopupMenu.render(stack);
        FiguraMod.popProfiler();

        //get avatar
        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);

        if (avatar != null) {
            //hud parent type
            avatar.hudRender(stack, this.minecraft.renderBuffers().bufferSource(), entity, tickDelta);

            //hud hidden by script
            if (avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderHUD) {
                //render figura overlays
                figura$renderOverlays(stack);
                //cancel this method
                ci.cancel();
            }
        }

        FiguraMod.popProfiler();
    }

    @Inject(at = @At("RETURN"), method = "render")
    private void afterRender(PoseStack stack, float tickDelta, CallbackInfo ci) {
        if (!AvatarManager.panic)
            figura$renderOverlays(stack);
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(PoseStack stack, CallbackInfo ci) {
        crosshairOffset = null;

        if (ActionWheel.isEnabled()) {
            ci.cancel();
            return;
        }

        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar;
        if (entity == null || (avatar = AvatarManager.getAvatar(entity)) == null || avatar.luaRuntime == null)
            return;

        RendererAPI renderer = avatar.luaRuntime.renderer;
        if (!renderer.renderCrosshair) {
            ci.cancel();
            return;
        }

        crosshairOffset = renderer.crosshairOffset;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V"), method = "renderCrosshair")
    private void blitRenderCrosshair(PoseStack stack, CallbackInfo ci) {
        if (crosshairOffset != null) {
            stack.pushPose();
            stack.translate(crosshairOffset.x, crosshairOffset.y, 0d);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", shift = At.Shift.AFTER), method = "renderCrosshair")
    private void afterBlitRenderCrosshair(PoseStack stack, CallbackInfo ci) {
        if (crosshairOffset != null)
            stack.popPose();
    }

    @Intrinsic
    private void figura$renderOverlays(PoseStack stack) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);

        //render aperdoll
        FiguraMod.pushProfiler("paperdoll");
        PaperDoll.render(stack, false);

        //render wheel
        FiguraMod.popPushProfiler("actionWheel");
        ActionWheel.render(stack);

        FiguraMod.popProfiler(2);
    }
}
