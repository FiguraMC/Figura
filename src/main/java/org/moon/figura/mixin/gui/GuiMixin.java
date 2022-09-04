package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.Entity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.ActionWheel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

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
        if (ActionWheel.isEnabled())
            ci.cancel();

        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar;
        if (entity != null && (avatar = AvatarManager.getAvatar(entity)) != null && avatar.luaRuntime != null && !avatar.luaRuntime.renderer.renderCrosshair)
            ci.cancel();
    }
}
