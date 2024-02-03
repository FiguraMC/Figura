package org.figuramc.figura.ducks.extensions;

import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;

public interface FontExtension {
    public void figura$drawInBatch8xOutline(FormattedCharSequence text, float x, float y, int color, int outlineColor, Matrix4f matrix, MultiBufferSource vertexConsumers, int light);
}
