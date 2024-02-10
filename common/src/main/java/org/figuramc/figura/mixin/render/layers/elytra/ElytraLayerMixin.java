package org.figuramc.figura.mixin.render.layers.elytra;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.PlatformUtils;
import org.figuramc.figura.utils.RenderUtils;
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
    @Shadow @Final private static ResourceLocation WINGS_LOCATION;
    @Unique
    private VanillaPart vanillaPart;
    @Unique
    private Avatar figura$avatar;

    @Unique
    private boolean renderedPivot;

    @Inject(at = @At(value = "HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void setAvatar(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        figura$avatar = AvatarManager.getAvatar(livingEntity);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ElytraModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V")
    public void onRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        vanillaPart = null;
        if (figura$avatar == null)
            return;

        if (figura$avatar.luaRuntime != null) {
            VanillaPart part = figura$avatar.luaRuntime.vanilla_model.ELYTRA;
            part.save(elytraModel);
            if (figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                vanillaPart = part;
                vanillaPart.preTransform(elytraModel);
            }
        }

        figura$avatar.elytraRender(livingEntity, multiBufferSource, poseStack, light, tickDelta, elytraModel);

        if (vanillaPart != null)
            vanillaPart.posTransform(elytraModel);

    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ElytraModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", cancellable = true)
    public void cancelVanillaPart(PoseStack poseStack, MultiBufferSource multiBufferSource,  int light, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (vanillaPart != null)
            vanillaPart.restore(elytraModel);
        renderedPivot = true;

        renderElytraPivot(poseStack, multiBufferSource, light, livingEntity, limbAngle, limbDistance, tickDelta, animationProgress);
        if (renderedPivot) {
            poseStack.popPose();
            ci.cancel();
        }
    }

    public void renderElytraPivot(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress) {

        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
        if (!itemStack.is(Items.ELYTRA) && !PlatformUtils.isModLoaded("origins")) {
            return;
        }
        if (figura$avatar != null && figura$avatar.luaRuntime != null && figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1 && figura$avatar.luaRuntime.vanilla_model.ELYTRA.checkVisible()) {
            // Try to render the pivot part
            this.elytraModel.setupAnim(livingEntity, limbAngle, limbDistance, tickDelta, light, animationProgress);

            VanillaPart part = RenderUtils.pivotToPart(figura$avatar, ParentType.LeftElytraPivot);

            ResourceLocation resourceLocation = livingEntity instanceof AbstractClientPlayer abstractClientPlayer ? (abstractClientPlayer.isElytraLoaded() && abstractClientPlayer.getElytraTextureLocation() != null ? abstractClientPlayer.getElytraTextureLocation() : (abstractClientPlayer.isCapeLoaded() && abstractClientPlayer.getCloakTextureLocation() != null && abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE) ? abstractClientPlayer.getCloakTextureLocation() : WINGS_LOCATION)) : WINGS_LOCATION;
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(resourceLocation), false, itemStack.hasFoil());

            if (part != null && part.checkVisible()) {
                boolean leftWing = figura$avatar.pivotPartRender(ParentType.LeftElytraPivot, stack -> {
                    stack.pushPose();
                    stack.scale(16, 16, 16);
                    stack.mulPose(Axis.XP.rotationDegrees(180f));
                    stack.mulPose(Axis.YP.rotationDegrees(180f));
                    stack.translate(0.0f, 0.0f, 0.125f);

                    ((ElytraModelAccessor)this.elytraModel).getLeftWing().render(stack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                    stack.popPose();
                });
                if (!leftWing) {
                    ((ElytraModelAccessor)this.elytraModel).getLeftWing().render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
            part = RenderUtils.pivotToPart(figura$avatar, ParentType.RightElytraPivot);
            if (part != null && part.checkVisible()) {
                    boolean rightWing = figura$avatar.pivotPartRender(ParentType.RightElytraPivot, stack -> {
                    stack.pushPose();
                    stack.scale(16, 16, 16);
                    stack.mulPose(Axis.XP.rotationDegrees(180f));
                    stack.mulPose(Axis.YP.rotationDegrees(180f));
                    stack.translate(0.0f, 0.0f, 0.125f);

                    ((ElytraModelAccessor)this.elytraModel).getRightWing().render(stack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                    stack.popPose();
                });
                if (!rightWing) {
                    ((ElytraModelAccessor)this.elytraModel).getRightWing().render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        } else renderedPivot = figura$avatar != null && figura$avatar.luaRuntime != null && figura$avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1 && !figura$avatar.luaRuntime.vanilla_model.ELYTRA.checkVisible();
    }
}
