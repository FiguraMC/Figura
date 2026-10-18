package org.figuramc.figura.mixin.render.feature;

import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRendererDispatcherMixin {
    @Unique
    private final FiguraFeatureRenderer figura$featureRenderer = new FiguraFeatureRenderer();

    @Inject(method = "prepareFrame", at = @At("HEAD"))
    private void figura$submitFeatures(SubmitNodeStorage storage,
            CallbackInfoReturnable<FeatureRenderDispatcher.PreparedFrame> cir) {
        // Avatar callbacks may submit additional avatars and add render orders.
        boolean pending;
        do {
            pending = false;
            for (var collection : List.copyOf(storage.getSubmitsPerOrder().values())) {
                if (!((NodeCollectorExtension) collection).getFiguraSubmissions().isEmpty()) {
                    pending = true;
                    figura$featureRenderer.render(collection, (SubmitNodeCollector) (Object) collection);
                }
            }
        } while (pending);
    }
}
