package org.figuramc.figura.mixin.render.nodeRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BiFunction;

@Mixin(SubmitNodeStorage.ModelPartSubmit.class)
public class SubmitNodeStorage$ModelPartSubmitMixin implements FiguraSubmitCallBackExtension {
    @Unique
    private BiFunction<MultiBufferSource, PoseStack, Boolean> figura$preRenderingCallback;
    @Unique
    private Runnable figura$postRenderingCallback;

    @Override
    public void figura$setPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback) {
        this.figura$preRenderingCallback = callback;
    }

    @Override
    public void figura$setPostRenderingCallback(Runnable callback) {
        this.figura$postRenderingCallback = callback;
    }

    @Override
    public BiFunction<MultiBufferSource, PoseStack, Boolean> figura$getPreRenderingCallback() {
        return figura$preRenderingCallback;
    }

    @Override
    public Runnable figura$getPostRenderingCallback() {
        return figura$postRenderingCallback;
    }
}
