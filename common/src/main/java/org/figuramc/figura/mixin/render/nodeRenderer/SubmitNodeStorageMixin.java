package org.figuramc.figura.mixin.render.nodeRenderer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraSubmission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SubmitNodeStorage.class)
public abstract class SubmitNodeStorageMixin implements NodeCollectorExtension {

    @Shadow
    public abstract SubmitNodeCollection order(int i);

    @Inject(method = "order(I)Lnet/minecraft/client/renderer/SubmitNodeCollection;", at = @At("RETURN"))
    private void figura$bindStorage(int order, CallbackInfoReturnable<SubmitNodeCollection> cir) {
        ((NodeCollectorExtension) cir.getReturnValue()).figura$setStorage((SubmitNodeStorage) (Object) this);
    }

    @Override
    public <S extends EntityRenderState> void submitFiguraModel(Avatar avatar, S renderState, TriFunction<Avatar, S, SubmitNodeCollector, Void> renderer) {
        NodeCollectorExtension extension = (NodeCollectorExtension) this.order(0);
        extension.submitFiguraModel(avatar, renderState, renderer);
    }

    // dummy
    @Override
    public List<FiguraSubmission> getFiguraSubmissions() {
        return List.of();
    }
}
