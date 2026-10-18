package org.figuramc.figura.mixin.render.feature;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(Model.Simple.class)
public class ModelPartFeatureRendererMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void figura$transferPartCallbacks(ModelPart part, Function<Identifier, RenderType> renderType,
                                             CallbackInfo ci) {
        FiguraSubmitCallBackExtension source = (FiguraSubmitCallBackExtension) (Object) part;
        FiguraSubmitCallBackExtension target = (FiguraSubmitCallBackExtension) this;
        for (var callback : source.figura$getPreRenderingCallbacks())
            target.figura$addPreRenderingCallback(callback);
        for (var callback : source.figura$getPostRenderingCallbacks())
            target.figura$addPostRenderingCallback(callback);
        target.figura$setPreventAnimSetup(source.figura$getPreventAnimSetup());
        source.figura$getPreRenderingCallbacks().clear();
        source.figura$getPostRenderingCallbacks().clear();
        source.figura$setPreventAnimSetup(false);
    }
}