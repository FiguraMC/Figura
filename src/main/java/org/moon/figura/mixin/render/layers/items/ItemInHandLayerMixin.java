package org.moon.figura.mixin.render.layers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (itemStack.isEmpty())
            return;

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null || TrustManager.get(livingEntity.getUUID()).get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        boolean left = humanoidArm == HumanoidArm.LEFT;

        //pivot part
        if (avatar.renderer != null) {
            PoseStack stack = avatar.renderer.pivotCustomizations.remove(left ? ParentType.LeftItemPivot : ParentType.RightItemPivot);
            if (stack != null) {
                stack.scale(16, 16, 16);
                stack.mulPose(Vector3f.XP.rotationDegrees(-90f));
                this.itemInHandRenderer.renderItem(livingEntity, itemStack, transformType, left, stack, multiBufferSource, i);
                ci.cancel();
                return;
            }
        }

        //vanilla part (script)
        if (avatar.luaState != null &&
                (left && !avatar.luaState.vanillaModel.LEFT_ITEM.isVisible() ||
                !left && !avatar.luaState.vanillaModel.RIGHT_ITEM.isVisible())
        ) {
            ci.cancel();
        }
    }
}
