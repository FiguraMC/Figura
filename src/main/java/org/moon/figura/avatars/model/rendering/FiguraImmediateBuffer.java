package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.BufferUtils;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.utils.caching.CacheStack;

import java.nio.FloatBuffer;

public class FiguraImmediateBuffer {

    private final FiguraTextureSet textureSet;
    private final CacheStack<PartCustomization, PartCustomization> customizationStack;
    public final FloatBuffer positions, uvs, normals;

    private FiguraImmediateBuffer(FloatArrayList posList, FloatArrayList uvList, FloatArrayList normalList, FiguraTextureSet textureSet, PartCustomization.Stack customizationStack) {
        positions = BufferUtils.createFloatBuffer(posList.size());
        positions.put(posList.toArray(new float[0]));
        uvs = BufferUtils.createFloatBuffer(uvList.size());
        uvs.put(uvList.toArray(new float[0]));
        normals = BufferUtils.createFloatBuffer(normalList.size());
        normals.put(normalList.toArray(new float[0]));
        this.textureSet = textureSet;
        this.customizationStack = customizationStack;
    }

    public void clean() {
        textureSet.clean();
    }

    public void uploadTexIfNeeded() {
        textureSet.uploadIfNeeded();
    }

    private static final FiguraVec4 pos = FiguraVec4.of();
    private static final FiguraVec3 normal = FiguraVec3.of();
    private static final FiguraVec3 uv = FiguraVec3.of(0, 0, 1);

    public void markBuffers() {
        positions.mark();
        uvs.mark();
        normals.mark();
    }

    public void resetBuffers() {
        positions.reset();
        uvs.reset();
        normals.reset();
    }

    public void clearBuffers() {
        positions.clear();
        uvs.clear();
        normals.clear();
    }

    /**
     * Advances the buffers without drawing those vertices. Also refunds complexity for those faces.
     * @param faceCount The number of faces to skip
     * @param remainingComplexity The complexity holder, so the value can update
     */
    public void advanceBuffers(int faceCount, int[] remainingComplexity) {
        positions.position(positions.position() + faceCount * 12);
        uvs.position(uvs.position() + faceCount * 8);
        normals.position(normals.position() + faceCount * 12);

        //Refund complexity for invisible parts
        remainingComplexity[0] += faceCount;
    }


    public void pushVertices(MultiBufferSource bufferSource, int overlay, int faceCount, int[] remainingComplexity) {
        //Handle cases that we can quickly
        if (faceCount == 0)
            return;
        if (!customizationStack.peek().visible) {
            advanceBuffers(faceCount, remainingComplexity);
            return;
        }

        RenderType primary = textureSet.getRenderType(customizationStack.peek().getPrimaryRenderType());
        RenderType secondary = textureSet.getRenderType(customizationStack.peek().getSecondaryRenderType());
        if (primary != null) {
            if (secondary != null)
                markBuffers();
            pushToConsumer(bufferSource.getBuffer(primary), overlay, faceCount);
        }
        if (secondary != null) {
            if (primary != null)
                resetBuffers();
            pushToConsumer(bufferSource.getBuffer(secondary), overlay, faceCount);
        }
    }

    private void pushToConsumer(VertexConsumer consumer, int overlay, int faceCount) {
        PartCustomization customization = customizationStack.peek();

        FiguraVec3 uvFixer = FiguraVec3.of();
        uvFixer.set(textureSet.getWidth(), textureSet.getHeight(), 1); //Dividing by this makes uv 0 to 1

        for (int i = 0; i < faceCount*4; i++) {

            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.multiply(customization.positionMatrix);
            normal.set(normals.get(), normals.get(), normals.get());
            normal.multiply(customization.normalMatrix);
            uv.set(uvs.get(), uvs.get(), 1);
            uv.divide(uvFixer);
            uv.multiply(customization.uvMatrix);

            consumer.vertex(
                    (float) pos.x,
                    (float) pos.y,
                    (float) pos.z,

                    (float) customization.color.x,
                    (float) customization.color.y,
                    (float) customization.color.z,
                    customization.alpha,

                    (float) uv.x,
                    (float) uv.y,

                    overlay,
                    customization.light,

                    (float) normal.x,
                    (float) normal.y,
                    (float) normal.z
            );
        }

        uvFixer.free();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int size;
        private final FloatArrayList positions = new FloatArrayList();
        private final FloatArrayList uvs = new FloatArrayList();
        private final FloatArrayList normals = new FloatArrayList();

        public Builder vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            positions.add(x);
            positions.add(y);
            positions.add(z);
            uvs.add(u);
            uvs.add(v);
            normals.add(nx);
            normals.add(ny);
            normals.add(nz);
            size++;
            return this;
        }

        public Builder vertex(FiguraVec3 pos, FiguraVec2 uv, FiguraVec3 normal) {
            return vertex((float) pos.x, (float) pos.y, (float) pos.z,
                    (float) uv.x, (float) uv.y,
                    (float) normal.x, (float) normal.y, (float) normal.z);
        }

        public int getSize() {
            return size;
        }

        public FiguraImmediateBuffer build(FiguraTextureSet textureSet, PartCustomization.Stack customizationStack) {
            return new FiguraImmediateBuffer(positions, uvs, normals, textureSet, customizationStack);
        }
    }

}
