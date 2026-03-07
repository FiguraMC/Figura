package org.figuramc.figura.ducks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

// this is used to set callbacks for pre and post rendering for Models, Model Parts, and other things
public interface FiguraSubmitCallBackExtension {
    void figura$addPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback);
    default List<BiFunction<MultiBufferSource, PoseStack, Boolean>> figura$getPreRenderingCallbacks() {
        return List.of();
    }
    void figura$addPostRenderingCallback(Runnable callback);
    default List<Runnable> figura$getPostRenderingCallbacks() {
        return List.of();
    }

    default boolean figura$getPreventAnimSetup() {
        return false;
    }

    default void figura$setPreventAnimSetup(boolean prevent) {

    }
}
