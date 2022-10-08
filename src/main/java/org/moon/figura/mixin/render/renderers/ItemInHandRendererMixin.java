package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.lua.api.vanilla_model.VanillaModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow protected abstract void renderPlayerArm(PoseStack matrices, MultiBufferSource vertexConsumers, int light, float equipProgress, float swingProgress, HumanoidArm arm);

    @Unique private Avatar avatar;
    @Unique private boolean canRender;
    @Unique private PlayerRenderer playerRenderer;

    @Inject(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderMainHand:Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void preRender(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo ci, float f, InteractionHand interactionHand, float g, @Coerce HandRenderSelectionAccessor handRenderSelection, float h, float i) {
        playerRenderer = null;

        canRender = handRenderSelection.renderMainHand() || handRenderSelection.renderOffHand();
        if (!canRender)
            return;

        avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        if (avatar != null)
            playerRenderer = (PlayerRenderer) this.entityRenderDispatcher.getRenderer(player);
    }

    @Inject(method = "renderHandsWithItems", at = @At("RETURN"))
    private void posRender(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo ci) {
        if (canRender && avatar != null && avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.PLAYER.restore(playerRenderer.getModel());
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void renderArmWithItem(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if (avatar == null || avatar.luaRuntime == null || item.isEmpty())
            return;

        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        VanillaModelPart part = arm == HumanoidArm.LEFT ? avatar.luaRuntime.vanilla_model.LEFT_ITEM : avatar.luaRuntime.vanilla_model.RIGHT_ITEM;

        if (!part.getVisible()) {
            ci.cancel();
            if (!player.isInvisible()) {
                matrices.pushPose();
                this.renderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
                matrices.popPose();
            }
        }
    }
}
