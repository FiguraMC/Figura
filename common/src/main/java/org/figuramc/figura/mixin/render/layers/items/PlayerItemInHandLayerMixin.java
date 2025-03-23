package org.figuramc.figura.mixin.render.layers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class only exists because of spyglass jank.
 * Has literally the exact same code as ItemInHandLayerMixin, just for the spyglass specifically.
 * For now, at least. Once spyglass category part exists, it may be different.
 * @param <S>
 * @param <M>
 */
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin <S extends PlayerRenderState, M extends EntityModel<S> & ArmedModel & HeadedModel> extends ItemInHandLayer<S, M> {

    public PlayerItemInHandLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Unique
    S figura$renderState;

    @Inject(method = "renderArmWithItem(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    void captureState(S playerRenderState, ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack matrices, MultiBufferSource vertexConsumers, int i, CallbackInfo ci) {
        this.figura$renderState = playerRenderState;
    }

    @Inject(method = "renderItemHeldToEye", at = @At("HEAD"), cancellable = true)
    private void adjustSpyglassVisibility(ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if (itemStackRenderState.isEmpty())
            return;

        boolean left = humanoidArm == HumanoidArm.LEFT;

        Avatar avatar = AvatarManager.getAvatar(figura$renderState);
        if (!RenderUtils.renderArmItem(avatar, left, ci))
            return;

        // pivot part
        if (avatar.pivotPartRender(left ? ParentType.LeftSpyglassPivot : ParentType.RightSpyglassPivot, stack -> {
            // spyglass code is weird - might need a fix, however it will break with non-humanoid avatars
            float s = 10f;
            stack.scale(s, s, s);
            stack.translate(0, 0, 7 / 16f);
            ItemTransform transform = ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$getItemTransform();
            if (!avatar.itemRenderEvent(ItemStackAPI.verify(((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$getItemStack()), ((FiguraItemStackRenderStateExtension)itemStackRenderState).figura$getDisplayContext().name(), FiguraVec3.fromVec3f(transform.translation()), FiguraVec3.of(transform.rotation().z(), transform.rotation().y(), transform.rotation().x()), FiguraVec3.fromVec3f(transform.scale()), ((FiguraItemStackRenderStateExtension) itemStackRenderState).figura$isLeftHanded(), stack, vertexConsumers, light, OverlayTexture.NO_OVERLAY))
                itemStackRenderState.render(stack, vertexConsumers, light, OverlayTexture.NO_OVERLAY);
        })) {
            ci.cancel();
        }
    }
}
