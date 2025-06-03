package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
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

    @Inject(method = "extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/special/PlayerHeadSpecialRenderer$PlayerHeadRenderInfo;", at = @At("HEAD"), require = 0)
    void saveTargetItem(ItemStack stack, CallbackInfoReturnable<ResolvableProfile> cir) {
        SkullBlockRendererAccessor.setItem(stack);
        figura$stack = stack;
    }


    @Inject(method = "render(Lnet/minecraft/client/renderer/special/PlayerHeadSpecialRenderer$PlayerHeadRenderInfo;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;renderSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;)V"), require = 0)
    void setTargetItem(PlayerHeadSpecialRenderer.PlayerHeadRenderInfo playerHeadRenderInfo, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, boolean bl, CallbackInfo ci) {
        SkullBlockRendererAccessor.setItem(figura$stack);
    }


    @Override
    public ItemStack figura$getItemStack() {
        return figura$stack;
    }
}
