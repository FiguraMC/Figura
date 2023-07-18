package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.jetbrains.annotations.Nullable;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entity, int seed);

    @Inject(at = @At("HEAD"), method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", cancellable = true)
    private void renderStatic(LivingEntity entity, ItemStack item, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, Level world, int light, int overlay, int seed, CallbackInfo ci) {
        if (entity == null || item.isEmpty())
            return;

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null)
            return;

        BakedModel bakedModel = this.getModel(item, world, entity, seed);
        ItemTransform transform = bakedModel.getTransforms().getTransform(renderMode);

        if (avatar.itemRenderEvent(ItemStackAPI.verify(item), renderMode.name(), FiguraVec3.fromVec3f(new Vector3f(transform.translation.x(), transform.translation.y(), transform.translation.z())), FiguraVec3.of(transform.rotation.z(), transform.rotation.y(), transform.rotation.x()), FiguraVec3.fromVec3f(new Vector3f(transform.scale.x(), transform.scale.y(), transform.scale.z())), leftHanded, matrices, vertexConsumers, light, overlay))
            ci.cancel();
    }
}
