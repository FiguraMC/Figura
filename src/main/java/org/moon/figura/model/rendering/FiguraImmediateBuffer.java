package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.utils.caching.CacheStack;

import java.nio.FloatBuffer;

public class FiguraImmediateBuffer {

    private final FiguraTextureSet textureSet;
    private final CacheStack<FiguraMat4, FiguraMat4> positionMatrixStack = new FiguraMat4.Stack();
    private final CacheStack<FiguraMat3, FiguraMat3> normalMatrixStack = new FiguraMat3.Stack();
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

    public void pushTransform(FiguraMat4 positionMat, FiguraMat3 normalMat) {
        positionMatrixStack.push(positionMat);
        normalMatrixStack.push(normalMat);
    }

    public void popTransform() {
        positionMatrixStack.pop();
        normalMatrixStack.pop();
    }

    public void checkTransformStackEmpty() {
        if (!positionMatrixStack.isEmpty() || !normalMatrixStack.isEmpty())
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

    private void advanceBuffers(int faceCount) {
        positions.position(positions.position() + faceCount * 12);
        uvs.position(uvs.position() + faceCount * 8);
        normals.position(normals.position() + faceCount * 12);
    }

    public void pushVertices(MultiBufferSource bufferSource, int light, int overlay, int faceCount, String renderTypeName) {
        RenderType renderType = textureSet.getRenderType(renderTypeName);
        if (renderType != null)
            pushToConsumer(bufferSource.getBuffer(renderType), light, overlay, faceCount);
        else
            advanceBuffers(faceCount);
    }

    private void pushToConsumer(VertexConsumer consumer, int light, int overlay, int faceCount) {
        FiguraMat4 posMat = positionMatrixStack.peek();
        FiguraMat3 normalMat = normalMatrixStack.peek();

        for (int i = 0; i < faceCount*4; i++) {

            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.multiply(posMat);
            normal.set(normals.get(), normals.get(), normals.get());
            normal.multiply(normalMat);
            uv.set(uvs.get(), uvs.get());
            if (textureSet.mainTex != null)
                uv.divide(textureSet.mainTex.getWidth(), textureSet.mainTex.getHeight());
            else if (textureSet.emissiveTex != null)
                uv.divide(textureSet.emissiveTex.getWidth(), textureSet.emissiveTex.getHeight());
            else
                throw new IllegalStateException("Texture set has neither emissive or main texture!?");

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
