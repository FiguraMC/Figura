package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.trust.TrustContainer;
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
public class ItemInHandRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Unique private Avatar avatar;
    @Unique private boolean canRender;
    @Unique private PlayerRenderer playerRenderer;

    @Inject(method = "renderHandsWithItems", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderMainHand:Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void preRender(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo ci, float f, InteractionHand interactionHand, float g, @Coerce HandRenderSelectionAccessor handRenderSelection, float h, float i) {
        canRender = handRenderSelection.renderMainHand() || handRenderSelection.renderOffHand();
        if (!canRender)
            return;

        avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        if (avatar == null)
            return;

        playerRenderer = (PlayerRenderer) this.entityRenderDispatcher.getRenderer(player);

        if (avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.PLAYER.store(playerRenderer.getModel());

        avatar.renderEvent(tickDelta);
    }

    @Inject(method = "renderHandsWithItems", at = @At("RETURN"))
    private void posRender(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo ci) {
        if (avatar == null || !canRender)
            return;

        avatar.postRenderEvent(tickDelta);

        if (avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.PLAYER.restore(playerRenderer.getModel());
    }
}
