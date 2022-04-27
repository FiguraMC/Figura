package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.utils.caching.CacheStack;

import java.nio.FloatBuffer;

public class FiguraImmediateBuffer {

    private final FiguraTextureSet textureSet;
    private final CacheStack<PartCustomization, PartCustomization> customizationStack = new PartCustomization.Stack();
    public final FloatBuffer positions, uvs, normals;

    private FiguraImmediateBuffer(FloatArrayList posList, FloatArrayList uvList, FloatArrayList normalList, FiguraTextureSet textureSet) {
        positions = BufferUtils.createFloatBuffer(posList.size());
        positions.put(posList.toArray(new float[0]));
        uvs = BufferUtils.createFloatBuffer(uvList.size());
        uvs.put(uvList.toArray(new float[0]));
        normals = BufferUtils.createFloatBuffer(normalList.size());
        normals.put(normalList.toArray(new float[0]));
        this.textureSet = textureSet;
    }

    public void clean() {
        customizationStack.fullClear();
        textureSet.clean();
    }

    public void pushCustomization(PartCustomization customization) {
        customizationStack.push(customization);
    }

    public void popCustomization() {
        customizationStack.pop();
    }

    public void checkEmpty() {
        if (!customizationStack.isEmpty())
            throw new IllegalStateException("Pushed matrices without popping them!");
    }

    public void uploadTexIfNeeded() {
        textureSet.uploadIfNeeded();
    }

    private static final FiguraVec4 pos = FiguraVec4.of();
    private static final FiguraVec3 normal = FiguraVec3.of();
    private static final FiguraVec2 uv = FiguraVec2.of();

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

    public void pushVertices(MultiBufferSource bufferSource, int light, int overlay, int faceCount) {
        RenderType primary = textureSet.getRenderType(customizationStack.peek().getPrimaryRenderType());
        RenderType secondary = textureSet.getRenderType(customizationStack.peek().getSecondaryRenderType());
        if (primary != null) {
            if (secondary != null)
                markBuffers();
            pushToConsumer(bufferSource.getBuffer(primary), light, overlay, faceCount);
        }
        if (secondary != null) {
            if (primary != null)
                resetBuffers();
            pushToConsumer(bufferSource.getBuffer(secondary), light, overlay, faceCount);
        }
    }

    private void pushToConsumer(VertexConsumer consumer, int light, int overlay, int faceCount) {
        PartCustomization customization = customizationStack.peek();

        FiguraVec2 uvFixer = FiguraVec2.of();
        if (textureSet.mainTex != null)
            uvFixer.set(textureSet.mainTex.getWidth(), textureSet.mainTex.getHeight());
        else if (textureSet.emissiveTex != null)
            uvFixer.set(textureSet.emissiveTex.getWidth(), textureSet.emissiveTex.getHeight());
        else
            throw new IllegalStateException("Texture set has neither emissive or main texture!?");

        for (int i = 0; i < faceCount*4; i++) {

            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.multiply(customization.positionMatrix);
            normal.set(normals.get(), normals.get(), normals.get());
            normal.multiply(customization.normalMatrix);
            uv.set(uvs.get(), uvs.get());
            uv.divide(uvFixer);

            consumer.vertex(
                    (float) pos.x,
                    (float) pos.y,
                    (float) pos.z,
                    1, 1, 1, 1,
                    (float) uv.x,
                    (float) uv.y,
                    overlay,
                    light,
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

        public FiguraImmediateBuffer build(FiguraTextureSet textureSet) {
            return new FiguraImmediateBuffer(positions, uvs, normals, textureSet);
        }
    }

}
