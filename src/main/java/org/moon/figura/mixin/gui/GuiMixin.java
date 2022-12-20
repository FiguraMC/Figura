package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.lua.api.RendererAPI;
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
    @Unique private boolean crosshair = false;

    @Inject(at = @At("RETURN"), method = "render")
    private void render(PoseStack stack, float tickDelta, CallbackInfo ci) {
        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar;
        if (entity == null || (avatar = AvatarManager.getAvatar(entity)) == null)
            return;

        avatar.hudRender(stack, minecraft.renderBuffers().bufferSource(), entity, tickDelta);
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(PoseStack stack, CallbackInfo ci) {
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

        if (renderer.crosshairOffset != null) {
            crosshair = true;
            stack.pushPose();
            stack.translate(renderer.crosshairOffset.x, renderer.crosshairOffset.y, 0d);
        }
    }

    @Inject(at = @At("RETURN"), method = "renderCrosshair")
    private void afterRenderCrosshair(PoseStack stack, CallbackInfo ci) {
        if (crosshair) {
            stack.popPose();
            crosshair = false;
        }
    }
}
