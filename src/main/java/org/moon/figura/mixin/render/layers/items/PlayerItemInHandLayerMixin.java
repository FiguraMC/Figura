package org.moon.figura.mixin.render.layers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * This class only exists because of spyglass jank.
 * Has literally the exact same code as ItemInHandLayerMixin, just for the spyglass specifically.
 * For now, at least. Once spyglass parent part exists, it may be different.
 * @param <T>
 * @param <M>
 */
@Mixin(PlayerItemInHandLayer.class)
public abstract class PlayerItemInHandLayerMixin <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {

    public PlayerItemInHandLayerMixin(RenderLayerParent<T, M> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
        super(renderLayerParent, itemInHandRenderer);
    }

    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    @Inject(method = "renderArmWithSpyglass", at = @At("HEAD"), cancellable = true)
    private void adjustSpyglassVisibility(LivingEntity livingEntity, ItemStack itemStack, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (itemStack.isEmpty())
            return;

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null || TrustManager.get(livingEntity.getUUID()).get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        boolean left = humanoidArm == HumanoidArm.LEFT;

        //script hide
        if (avatar.luaState != null &&
                (left && !avatar.luaState.vanillaModel.LEFT_ITEM.isVisible() ||
                !left && !avatar.luaState.vanillaModel.RIGHT_ITEM.isVisible()
        )) {
            ci.cancel();
            return;
        }

        //pivot part
        if (avatar.renderer != null) {
            List<PoseStack> list = avatar.renderer.pivotCustomizations.get(left ? ParentType.LeftSpyglassPivot : ParentType.RightSpyglassPivot);
            if (list != null && !list.isEmpty()) {
                for (PoseStack stack : list) {
                    //spyglass code is weird - might need a fix, however it will break with non-humanoid avatars
                    stack.scale(10f, 10f, 10f);
                    stack.translate(0, 0, 7 / 16f);
                    this.itemInHandRenderer.renderItem(livingEntity, itemStack, ItemTransforms.TransformType.HEAD, false, stack, multiBufferSource, i);
                }
                list.clear();
                ci.cancel();
            }
        }
    }
}
