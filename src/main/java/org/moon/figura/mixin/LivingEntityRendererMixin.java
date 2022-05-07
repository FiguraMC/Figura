package org.moon.figura.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Unique
    private Avatar currentAvatar;

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void preRender(LivingEntity entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int l, CallbackInfo ci) {
        currentAvatar = AvatarManager.getAvatar(entity);
        if (currentAvatar == null)
            return;
        EntityModel<?> model = ((LivingEntityRenderer<?, ?>) (Object) this).getModel();
        if (model instanceof PlayerModel<?> playerModel)
            if (currentAvatar.luaState != null)
                currentAvatar.luaState.vanillaModel.alterModel(playerModel);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void endRender(LivingEntity entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int l, CallbackInfo ci) {
        if (currentAvatar == null)
            return;
        //Render avatar with params
        EntityModel<?> model = ((LivingEntityRenderer<?, ?>) (Object) this).getModel();
        currentAvatar.onRender(entity, yaw, delta, matrices, bufferSource, l, model);
        if (model instanceof PlayerModel<?> playerModel)
            if (currentAvatar.luaState != null)
                currentAvatar.luaState.vanillaModel.alterModel(playerModel);
        currentAvatar = null;
    }

}
