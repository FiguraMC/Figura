package org.moon.figura.mixin.render.layers.elytra;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow @Final private ElytraModel<T> elytraModel;
    private VanillaModelAPI vanillaModelAPI;

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void onRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar != null && avatar.luaState != null)
            vanillaModelAPI = avatar.luaState.vanillaModel;
        else
            vanillaModelAPI = null;

        if (vanillaModelAPI != null) {
            vanillaModelAPI.copyByPart(elytraModel, vanillaModelAPI.LEFT_ELYTRA);
            vanillaModelAPI.copyByPart(elytraModel, vanillaModelAPI.RIGHT_ELYTRA);
            if (TrustManager.get(livingEntity.getUUID()).get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 1) {
                vanillaModelAPI.alterByPart(elytraModel, vanillaModelAPI.LEFT_ELYTRA);
                vanillaModelAPI.alterByPart(elytraModel, vanillaModelAPI.RIGHT_ELYTRA);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void postRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (vanillaModelAPI != null)
            vanillaModelAPI.restoreByPart(elytraModel, vanillaModelAPI.ELYTRA);
    }
}
