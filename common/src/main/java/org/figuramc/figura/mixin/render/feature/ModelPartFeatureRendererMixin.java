package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

// this method runs callbacks for Models before and after rendering, as well as before animation setup if they exist
@Mixin(ModelPartFeatureRenderer.class)
public class ModelPartFeatureRendererMixin {
    @Shadow
    @Final
    private PoseStack poseStack;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0), cancellable = true)
    private <S> void figura$preRender(Map<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> modelPartSubmits, MultiBufferSource.BufferSource bufferSource,
                                      OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource crumblingBufferSource, CallbackInfo ci, @Local SubmitNodeStorage.ModelPartSubmit modelSubmit) {
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0, shift = At.Shift.AFTER))
    private <S> void figura$postRender(Map<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> modelPartSubmits, MultiBufferSource.BufferSource bufferSource,
                                       OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource crumblingBufferSource, CallbackInfo ci, @Local SubmitNodeStorage.ModelPartSubmit modelSubmit) {
        FiguraSubmitCallBackExtension callBackExtension = (FiguraSubmitCallBackExtension) (Object) modelSubmit;
        for (var callback : callBackExtension.figura$getPostRenderingCallbacks())
             callback.run();
        callBackExtension.figura$getPostRenderingCallbacks().clear();
    }
}
