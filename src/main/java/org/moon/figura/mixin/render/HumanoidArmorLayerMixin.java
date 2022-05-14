package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.lua.api.model.VanillaModelAPI;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    private VanillaModelAPI vanillaModelAPI;

    @Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void onRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar != null && avatar.luaState != null && TrustManager.get(livingEntity.getUUID()).get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 1)
            vanillaModelAPI = avatar.luaState.vanillaModel;
        else
            vanillaModelAPI = null;
    }

    @Inject(at = @At("RETURN"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void postRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        vanillaModelAPI = null;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"), method = "renderArmorPiece")
    public void onRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel, CallbackInfo ci) {
        if (vanillaModelAPI != null) {
            vanillaModelAPI.alterByPart(humanoidModel,
                switch (equipmentSlot) {
                    case HEAD -> vanillaModelAPI.HELMET;
                    case CHEST -> vanillaModelAPI.CHESTPLATE;
                    case LEGS -> vanillaModelAPI.LEGGINGS;
                    case FEET -> vanillaModelAPI.BOOTS;
                    default -> null;
                });
        }
    }

    @Inject(at = @At("RETURN"), method = "renderArmorPiece")
    public void postRenderArmorPiece(PoseStack poseStack, MultiBufferSource multiBufferSource, T livingEntity, EquipmentSlot equipmentSlot, int i, A humanoidModel, CallbackInfo ci) {
        if (vanillaModelAPI != null) {
            vanillaModelAPI.restoreByPart(humanoidModel,
                switch (equipmentSlot) {
                    case HEAD -> vanillaModelAPI.HELMET;
                    case CHEST -> vanillaModelAPI.CHESTPLATE;
                    case LEGS -> vanillaModelAPI.LEGGINGS;
                    case FEET -> vanillaModelAPI.BOOTS;
                    default -> null;
                });
        }
    }

}
