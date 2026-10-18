package org.figuramc.figura.mixin.render.nodeRenderer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraFeatureRenderer;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraSubmission;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin implements NodeCollectorExtension, SubmitNodeCollector {
    @Unique
    private final List<FiguraSubmission> figuraSubmissions = new ArrayList<>();
    @Unique
    private SubmitNodeStorage figura$storage;

    @Override
    public void figura$setStorage(SubmitNodeStorage storage) {
        figura$storage = storage;
    }

    @Override
    public net.minecraft.client.renderer.OrderedSubmitNodeCollector order(int order) {
        // Vanilla storage orders are absolute, including nested item layers.
        return figura$storage == null ? (SubmitNodeCollection) (Object) this : figura$storage.order(order);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends EntityRenderState> void submitFiguraModel(Avatar avatar, S state,
            TriFunction<Avatar, S, SubmitNodeCollector, Void> renderer) {
        figuraSubmissions.add(new FiguraSubmission(avatar, state,
                (TriFunction<Avatar, EntityRenderState, SubmitNodeCollector, Void>) renderer));
    }

    @Override
    public List<FiguraSubmission> getFiguraSubmissions() {
        return figuraSubmissions;
    }

    @WrapMethod(method = "submitModel")
    private <S> void figura$submitModel(Model<? super S> model, S state, PoseStack pose,
            RenderType type, int light, int overlay, int color, UvMapping uv, int outline,
            Operation<Void> original) {
        FiguraSubmitCallBackExtension callbacks = (FiguraSubmitCallBackExtension) model;
        if (callbacks.figura$getPreRenderingCallbacks().isEmpty()
                && callbacks.figura$getPostRenderingCallbacks().isEmpty()
                && !callbacks.figura$getPreventAnimSetup()
                && !FiguraFeatureRenderer.isModelPrepared(model)) {
            original.call(model, state, pose, type, light, overlay, color, uv, outline);
            return;
        }
        Runnable submitSnapshot = () -> original.call(FiguraFeatureRenderer.snapshotModel(model),
                net.minecraft.util.Unit.INSTANCE, pose, type, light, overlay, color, uv, outline);
        if (FiguraFeatureRenderer.isModelPrepared(model))
            submitSnapshot.run();
        else
            FiguraFeatureRenderer.withModelCallbacks(model, state, this, pose, submitSnapshot);
    }
    @WrapMethod(method = "submitItem")
    private void figura$submitItem(PoseStack pose, ItemDisplayContext display, int light,
            int overlay, int outline, int[] tints, ItemQuads quads, ItemStackRenderState.FoilType foil,
            Operation<Void> original) {
        FiguraSubmitCallBackExtension callbacks = (FiguraSubmitCallBackExtension) (Object) display;
        var before = List.copyOf(callbacks.figura$getPreRenderingCallbacks());
        var after = List.copyOf(callbacks.figura$getPostRenderingCallbacks());
        callbacks.figura$getPreRenderingCallbacks().clear();
        callbacks.figura$getPostRenderingCallbacks().clear();
        try {
            boolean visible = true;
            for (var callback : before)
                visible &= callback.apply(this, pose);
            if (visible)
                original.call(pose, display, light, overlay, outline, tints, quads, foil);
        } finally {
            for (var callback : after)
                callback.run();
        }
    }
}
