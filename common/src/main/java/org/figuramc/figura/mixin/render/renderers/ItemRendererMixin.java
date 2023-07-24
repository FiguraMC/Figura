package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow public abstract BakedModel getModel(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entity, int seed);

    @Inject(at = @At("HEAD"), method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", cancellable = true)
    private void renderStatic(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean leftHanded, PoseStack stack, MultiBufferSource buffer, Level world, int light, int overlay, int seed, CallbackInfo ci) {
        if (livingEntity == null || itemStack.isEmpty())
            return;

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null)
            return;

        BakedModel bakedModel = this.getModel(itemStack, world, livingEntity, seed);
        ItemTransform transform = bakedModel.getTransforms().getTransform(itemDisplayContext);

        if (avatar.itemRenderEvent(ItemStackAPI.verify(itemStack), itemDisplayContext.name(), FiguraVec3.fromVec3f(transform.translation), FiguraVec3.of(transform.rotation.z, transform.rotation.y, transform.rotation.x), FiguraVec3.fromVec3f(transform.scale), leftHanded, stack, buffer, light, overlay))
            ci.cancel();
    }
}
