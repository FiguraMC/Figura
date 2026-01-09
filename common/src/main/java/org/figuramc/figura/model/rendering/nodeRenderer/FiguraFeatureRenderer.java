package org.figuramc.figura.model.rendering.nodeRenderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import org.figuramc.figura.ducks.NodeCollectorExtension;

import java.util.List;
import java.util.Map;

public class FiguraFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource) {
        List<FiguraSubmission> figuraSubmissions = ((NodeCollectorExtension) submitNodeCollection).getFiguraSubmissions();

        for (FiguraSubmission figuraSubmission : figuraSubmissions) {
            if (figuraSubmission.avatar() == null)
                continue;

            figuraSubmission.renderer().apply(
                    figuraSubmission.avatar(),
                    figuraSubmission.renderState(),
                    bufferSource
            );
        }
    }
}
