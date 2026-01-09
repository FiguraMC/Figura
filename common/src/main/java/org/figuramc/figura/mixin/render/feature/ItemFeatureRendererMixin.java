package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class ItemFeatureRendererMixin {

    @Shadow
    @Final
    private PoseStack poseStack;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderItem(Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II[ILjava/util/List;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V", ordinal = 0), cancellable = true)
    private void figura$preRender(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource,
                                  OutlineBufferSource outlineBufferSource, CallbackInfo ci, @Local SubmitNodeStorage.ItemSubmit itemSubmit) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) itemSubmit;

        boolean shouldCancel = callBackExtension.figura$getPreRenderingCallback() != null &&
                !callBackExtension.figura$getPreRenderingCallback().apply(bufferSource, poseStack);

        if (shouldCancel)
            ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderItem(Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II[ILjava/util/List;Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V", ordinal = 0, shift = At.Shift.AFTER))
    private <S> void figura$postRender(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource,
                                       OutlineBufferSource outlineBufferSource, CallbackInfo ci, @Local SubmitNodeStorage.ItemSubmit itemSubmit) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) itemSubmit;
        if (callBackExtension.figura$getPostRenderingCallback() != null)
            callBackExtension.figura$getPostRenderingCallback().run();
    }
}
