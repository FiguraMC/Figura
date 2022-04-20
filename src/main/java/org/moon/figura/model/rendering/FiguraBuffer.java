package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;

import java.nio.FloatBuffer;

public class FiguraBuffer {

    private final int vertexCount;
    private final FiguraMat4 transform = FiguraMat4.of();
    private final FiguraMat3 normalMat = FiguraMat3.of();
    public final FloatBuffer positions, uvs, normals;

    private FiguraBuffer(FloatArrayList posList, FloatArrayList uvList, FloatArrayList normalList) {
        vertexCount = posList.size() / 3;
        positions = BufferUtils.createFloatBuffer(posList.size());
        positions.put(posList.toArray(new float[0]));
        uvs = BufferUtils.createFloatBuffer(uvList.size());
        uvs.put(uvList.toArray(new float[0]));
        normals = BufferUtils.createFloatBuffer(normalList.size());
        normals.put(normalList.toArray(new float[0]));
    }

    public void setTransform(FiguraMat4 transform) {
        this.transform.set(transform);
    }

    public void setNormalMat(FiguraMat3 mat) {
        this.normalMat.set(mat);
    }

    private static final FiguraVec4 pos = FiguraVec4.of();
    private static final FiguraVec3 normal = FiguraVec3.of();

    public void pushToConsumer(VertexConsumer consumer, int light, int overlay) {
        positions.clear();
        uvs.clear();
        normals.clear();

        for (int i = 0; i < vertexCount; i++) {
            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.multiply(transform);
            normal.set(normals.get(), normals.get(), normals.get());
            normal.multiply(normalMat);
            consumer.vertex(
                    (float) pos.x,
                    (float) pos.y,
                    (float) pos.z,
                    1, 1, 1, 1,
                    uvs.get(),
                    uvs.get(),
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
            return this;
        }

        public Builder vertex(FiguraVec3 pos, FiguraVec2 uv, FiguraVec3 normal) {
            return vertex((float) pos.x, (float) pos.y, (float) pos.z,
                    (float) uv.x, (float) uv.y,
                    (float) normal.x, (float) normal.y, (float) normal.z);
        }

        public FiguraBuffer build() {
            return new FiguraBuffer(positions, uvs, normals);
        }
    }

}
