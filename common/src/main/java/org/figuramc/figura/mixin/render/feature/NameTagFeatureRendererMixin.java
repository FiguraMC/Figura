package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import org.figuramc.figura.ducks.NameTagFeatureRenderer$StorageExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NameTagFeatureRenderer.class)
public class NameTagFeatureRendererMixin {
    @Inject(method = "renderTranslucent", at = @At(value = "TAIL"))
    private void renderOutlineTexts(CallbackInfo ci, @Local(argsOnly = true) MultiBufferSource.BufferSource bufferSource,
                                    @Local(argsOnly = true) Font font, @Local NameTagFeatureRenderer.Storage storage) {
        NameTagFeatureRenderer$StorageExtension storageExt = (NameTagFeatureRenderer$StorageExtension) storage;
        for (SubmitNodeStorage.NameTagSubmit outlineText : storageExt.getOutlineSubmits()) {
            font.drawInBatch8xOutline(outlineText.text().getVisualOrderText(), outlineText.x(), outlineText.y(), outlineText.color(),
                    outlineText.backgroundColor(), outlineText.pose(), bufferSource, outlineText.lightCoords());
        }
    }
}
