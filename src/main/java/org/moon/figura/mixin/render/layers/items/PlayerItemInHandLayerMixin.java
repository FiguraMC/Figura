package org.moon.figura.mixin.render.layers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class only exists because of spyglass jank.
 * Has literally the exact same code as ItemInHandLayerMixin, just for the spyglass specifically.
 * For now, at least. Once spyglass parent part exists, it may be different.
 * @param <T>
 * @param <M>
 */
@Mixin(PlayerItemInHandLayer.class)
public class PlayerItemInHandLayerMixin <T extends Player, M extends EntityModel<T> & HeadedModel> {

    @Inject(method = "renderArmWithSpyglass", at = @At("HEAD"), cancellable = true)
    private void adjustSpyglassVisibility(LivingEntity livingEntity, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null || avatar.luaState == null)
            return;
        if (humanoidArm == HumanoidArm.LEFT && !avatar.luaState.vanillaModel.LEFT_ITEM.isVisible())
            ci.cancel();
        if (!avatar.luaState.vanillaModel.RIGHT_ITEM.isVisible())
            ci.cancel();
    }

}
