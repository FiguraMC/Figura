package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraItemRendererExtension;
import org.figuramc.figura.ducks.FiguraItemStackRenderStateExtension;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements FiguraItemRendererExtension {

    @Shadow @Final private ItemModelResolver resolver;

    @Shadow @Final private ItemStackRenderState scratchItemStackRenderState;

    @Inject(at = @At("HEAD"), method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", cancellable = true)
    private void renderStatic(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack stack, MultiBufferSource buffer, Level world, int light, int overlay, int seed, CallbackInfo ci) {
        if (livingEntity == null || itemStack.isEmpty())
            return;

        Avatar avatar = AvatarManager.getAvatar(livingEntity);
        if (avatar == null)
            return;

        this.resolver.updateForTopItem(this.scratchItemStackRenderState, itemStack, itemDisplayContext, world, livingEntity, seed);
        ItemTransform transform = ((FiguraItemStackRenderStateExtension)this.scratchItemStackRenderState).figura$getItemTransform();
        boolean leftHanded =  ((FiguraItemStackRenderStateExtension)this.scratchItemStackRenderState).figura$isLeftHanded();

        if (avatar.itemRenderEvent(ItemStackAPI.verify(itemStack), itemDisplayContext.name(), FiguraVec3.fromVec3f(transform.translation()), FiguraVec3.of(transform.rotation().z(), transform.rotation().y(), transform.rotation().x()), FiguraVec3.fromVec3f(transform.scale()), leftHanded, stack, buffer, light, overlay))
            ci.cancel();
    }

    @Unique
    public int figura$getModelComplexity(ItemStack stack, RandomSource randomSource) {
        this.resolver.updateForTopItem(this.scratchItemStackRenderState, stack, ItemDisplayContext.NONE, WorldAPI.getCurrentWorld(), null, 1);
        if (((FiguraItemStackRenderStateExtension)(this.scratchItemStackRenderState)).figura$getQuads() != null && !((FiguraItemStackRenderStateExtension) (this.scratchItemStackRenderState)).figura$getQuads().isEmpty())
            return ((FiguraItemStackRenderStateExtension)(this.scratchItemStackRenderState)).figura$getQuads().size();
        return 20;
    }
}
