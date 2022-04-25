package org.moon.figura.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(at = @At("HEAD"), method = "render")
    private void testDrawing(Entity entity, float yaw, float delta, PoseStack m, MultiBufferSource bufferSource, int l, CallbackInfo ci) {

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null)
            return;

        //Render avatar with params
        avatar.renderer.entity = entity;
        avatar.renderer.yaw = yaw;
        avatar.renderer.tickDelta = delta;
        avatar.renderer.matrices = m;
        avatar.renderer.light = l;
        avatar.renderer.bufferSource = bufferSource;

        avatar.renderer.render();
    }

}
