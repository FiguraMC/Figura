package org.figuramc.figura.ducks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.BiFunction;
import java.util.function.Function;

// this is used to set callbacks for pre and post rendering for Models, Model Parts, and other things
public interface FiguraSubmitCallBackExtension {
    void figura$setPreRenderingCallback(BiFunction<MultiBufferSource, PoseStack, Boolean> callback);
    default BiFunction<MultiBufferSource, PoseStack, Boolean> figura$getPreRenderingCallback() {
        return null;
    }
    void figura$setPostRenderingCallback(Runnable callback);
    default Runnable figura$getPostRenderingCallback() {
        return null;
    }

    default boolean figura$getPreventAnimSetup() {
        return false;
    }

    default void figura$setPreventAnimSetup(boolean prevent) {

    }
}
