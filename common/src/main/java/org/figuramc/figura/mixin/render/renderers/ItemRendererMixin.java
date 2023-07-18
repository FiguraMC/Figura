package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entity, int seed);

    @Inject(at = @At("HEAD"), method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", cancellable = true)
    private void renderStatic(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack stack, MultiBufferSource buffer, Level world, int light, int overlay, int seed, CallbackInfo ci) {
        if (livingEntity == null || itemStack.isEmpty())
            return;

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null)
            return;

        BakedModel bakedModel = this.getModel(itemStack, world, livingEntity, seed);
        ItemTransform transform = bakedModel.getTransforms().getTransform(renderMode);

        if (avatar.itemRenderEvent(ItemStackAPI.verify(itemStack), renderMode.name(), FiguraVec3.fromVec3f(transform.translation), FiguraVec3.of(transform.rotation.z(), transform.rotation.y(), transform.rotation.x()), FiguraVec3.fromVec3f(transform.scale), leftHanded, stack, buffer, light, overlay))
            ci.cancel();
    }
}
