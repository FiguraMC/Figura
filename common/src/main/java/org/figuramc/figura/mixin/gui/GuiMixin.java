package org.figuramc.figura.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.ActionWheel;
import org.figuramc.figura.gui.FiguraGui;
import org.figuramc.figura.lua.api.RendererAPI;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;
    @Unique private FiguraVec2 crosshairOffset;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void onRender(GuiGraphics guiGraphics, float tickDelta, CallbackInfo ci) {
        FiguraGui.onRender(guiGraphics, tickDelta, ci);
    }

    @Inject(at = @At("RETURN"), method = "render")
    private void afterRender(GuiGraphics guiGraphics, float tickDelta, CallbackInfo ci) {
        if (!AvatarManager.panic)
            FiguraGui.renderOverlays(guiGraphics);
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"), method = "renderCrosshair")
    private void blitRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (crosshairOffset != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(crosshairOffset.x, crosshairOffset.y, 0d);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", shift = At.Shift.AFTER), method = "renderCrosshair")
    private void afterBlitRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (crosshairOffset != null)
            guiGraphics.pose().popPose();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"), method = "renderCrosshair")
    private void blitRenderCrosshairSliced(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (crosshairOffset != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(crosshairOffset.x, crosshairOffset.y, 0d);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V", shift = At.Shift.AFTER), method = "renderCrosshair")
    private void afterBlitRenderCrosshairSliced(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (crosshairOffset != null)
            guiGraphics.pose().popPose();
    }
}
