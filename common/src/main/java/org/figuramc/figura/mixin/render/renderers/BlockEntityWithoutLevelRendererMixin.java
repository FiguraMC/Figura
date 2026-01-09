package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.figuramc.figura.ducks.SkullBlockRendererAccessor;
import org.figuramc.figura.ducks.SkullSpecialRendererExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerHeadSpecialRenderer.class)
public class BlockEntityWithoutLevelRendererMixin implements SkullSpecialRendererExtension {

    @Unique
    ItemStack figura$stack;

    @Inject(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;", at = @At("HEAD"), require = 0)
    void saveTargetItem(ItemStack stack, CallbackInfoReturnable<ResolvableProfile> cir) {
        SkullBlockRendererAccessor.setItem(stack);
        figura$stack = stack;
    }


    @Inject(method = "submit(Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;submitSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), require = 0)
    void setTargetItem(PlayerSkinRenderCache.RenderInfo renderInfo, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k, CallbackInfo ci) {
        SkullBlockRendererAccessor.setItem(figura$stack);
    }


    @Override
    public ItemStack figura$getItemStack() {
        return figura$stack;
    }
}
