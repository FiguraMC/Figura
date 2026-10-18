package org.figuramc.figura.ducks;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.apache.commons.lang3.function.TriFunction;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.model.rendering.nodeRenderer.FiguraSubmission;

import java.util.List;


@FunctionalInterface
interface QuadFunction<A, B, C, D, R> {
    R apply(A a, B b, C c, D d);
}

public interface NodeCollectorExtension {
    default void figura$setStorage(SubmitNodeStorage storage) {
    }

    <S extends EntityRenderState> void submitFiguraModel(Avatar avatar, S renderState, TriFunction<Avatar, S, SubmitNodeCollector, Void> renderer);

    List<FiguraSubmission> getFiguraSubmissions();
}

