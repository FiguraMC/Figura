package org.moon.figura.mixin.render.layers.elytra;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.lua.api.vanilla_model.VanillaPart;
import org.moon.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public ElytraLayerMixin(RenderLayerParent<T, M> context) {
        super(context);
    }

    @Shadow @Final private ElytraModel<T> elytraModel;
    @Unique
    private VanillaPart vanillaPart;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ElytraModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void onRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        vanillaPart = null;
        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null)
            return;

        if (avatar.luaRuntime != null) {
            VanillaPart part = avatar.luaRuntime.vanilla_model.ELYTRA;
            part.save(elytraModel);
            if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                vanillaPart = part;
                vanillaPart.preTransform(elytraModel);
            }
        }

        avatar.elytraRender(livingEntity, multiBufferSource, poseStack, light, tickDelta, elytraModel);

        if (vanillaPart != null)
            vanillaPart.posTransform(elytraModel);
    }

    @Inject(at = @At("RETURN"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void postRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (vanillaPart != null)
            vanillaPart.restore(elytraModel);
    }
}
