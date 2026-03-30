package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// this method runs callbacks for Models before and after rendering, as well as before animation setup if they exist
@Mixin(ModelFeatureRenderer.class)
public class ModelFeatureRendererMixin {
    @Shadow
    @Final
    private PoseStack poseStack;

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V"), cancellable = true)
    private <S> void figura$beforeSetupAnim(SubmitNodeStorage.ModelSubmit<S> modelSubmit, RenderType renderType, VertexConsumer vertexConsumer,
                                            OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) modelSubmit;
        if (callBackExtension.figura$getPreventAnimSetup())
            ci.cancel();
    }


    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0), cancellable = true)
    private <S> void figura$preRender(SubmitNodeStorage.ModelSubmit<S> modelSubmit, RenderType renderType, VertexConsumer vertexConsumer,
                                            OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) modelSubmit;

        for (var callback : callBackExtension.figura$getPreRenderingCallbacks()) {
            if (!callback.apply(bufferSource, poseStack)) {
                ci.cancel();
            }
        }

        if (ci.isCancelled()) {
            for (var callback : callBackExtension.figura$getPostRenderingCallbacks())
                callback.run();

            callBackExtension.figura$getPostRenderingCallbacks().clear();
        }

        callBackExtension.figura$getPreRenderingCallbacks().clear();
    }

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0, shift = At.Shift.AFTER))
    private <S> void figura$postRender(SubmitNodeStorage.ModelSubmit<S> modelSubmit, RenderType renderType, VertexConsumer vertexConsumer,
                                      OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) modelSubmit;
        for (var callback : callBackExtension.figura$getPostRenderingCallbacks())
            callback.run();

        callBackExtension.figura$getPostRenderingCallbacks().clear();
    }
}
