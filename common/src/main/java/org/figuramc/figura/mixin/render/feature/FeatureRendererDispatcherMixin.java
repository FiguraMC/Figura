package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraFeatureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRendererDispatcherMixin {
    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;
    final FiguraFeatureRenderer figuraFeatureRenderer = new FiguraFeatureRenderer();

    @Inject(method = "renderAllFeatures",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ShadowFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"))
    private void figura$renderFiguraFeatures(CallbackInfo ci, @Local SubmitNodeCollection submitNodeCollection) {
        figuraFeatureRenderer.render(submitNodeCollection, this.bufferSource);
    }
}
