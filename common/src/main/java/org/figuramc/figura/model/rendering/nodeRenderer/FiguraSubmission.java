package org.figuramc.figura.model.rendering.nodeRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;

public record FiguraSubmission(Avatar avatar, EntityRenderState renderState, TriFunction<Avatar, EntityRenderState, MultiBufferSource, Void> renderer) {

}
