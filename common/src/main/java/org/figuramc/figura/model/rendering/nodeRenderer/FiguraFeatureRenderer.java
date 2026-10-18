package org.figuramc.figura.model.rendering.nodeRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import org.figuramc.figura.ducks.NodeCollectorExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.Collections;

public class FiguraFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, SubmitNodeCollector bufferSource) {
        List<FiguraSubmission> figuraSubmissions = ((NodeCollectorExtension) submitNodeCollection).getFiguraSubmissions();

        List<FiguraSubmission> pending = List.copyOf(figuraSubmissions);
        figuraSubmissions.clear();
        for (FiguraSubmission figuraSubmission : pending) {
            if (figuraSubmission.avatar() == null)
                continue;

            var renderer = figuraSubmission.avatar().renderer;
            if (renderer != null && figuraSubmission.renderState() != null)
                renderer.outlineColor = figuraSubmission.renderState().outlineColor;

            figuraSubmission.renderer().apply(
                    figuraSubmission.avatar(),
                    figuraSubmission.renderState(),
                    bufferSource
            );
        }
    }
    private static final ThreadLocal<Set<Model<?>>> preparedModels =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    public static boolean isModelPrepared(Model<?> model) {
        return preparedModels.get().contains(model);
    }

    public static Model.Simple snapshotModel(Model<?> model) {
        return new Model.Simple(copyPart(model.root()), model.renderType());
    }

    private static ModelPart copyPart(ModelPart source) {
        Map<String, ModelPart> children = new LinkedHashMap<>();
        source.children.forEach((name, child) -> children.put(name, copyPart(child)));
        var copy = new ModelPart(source.cubes, children);
        copy.loadPose(source.storePose());
        copy.visible = source.visible;
        copy.skipDraw = source.skipDraw;
        return copy;
    }

    /** Keep Figura transforms active until every equipment layer and trim has been snapshotted. */
    public static <S> void withModelCallbacks(Model<? super S> model, S state,
            SubmitNodeCollector collector, PoseStack pose, Runnable submit) {
        var callbacks = (FiguraSubmitCallBackExtension) model;
        var before = List.copyOf(callbacks.figura$getPreRenderingCallbacks());
        var after = List.copyOf(callbacks.figura$getPostRenderingCallbacks());
        boolean prevent = callbacks.figura$getPreventAnimSetup();
        if (before.isEmpty() && after.isEmpty() && !prevent) {
            submit.run();
            return;
        }
        callbacks.figura$getPreRenderingCallbacks().clear();
        callbacks.figura$getPostRenderingCallbacks().clear();
        callbacks.figura$setPreventAnimSetup(false);
        boolean added = preparedModels.get().add(model);
        try {
            if (!prevent)
                model.setupAnim(state);
            boolean visible = true;
            for (var callback : before)
                visible &= callback.apply(collector, pose);
            if (visible)
                submit.run();
        } finally {
            if (added)
                preparedModels.get().remove(model);
            for (var callback : after)
                callback.run();
        }
    }
}
