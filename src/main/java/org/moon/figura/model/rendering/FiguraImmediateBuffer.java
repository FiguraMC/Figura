package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.model.rendering.texture.RenderTypes;
import org.moon.figura.utils.caching.CacheStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class FiguraImmediateBuffer {

    private final FiguraTextureSet textureSet;
    private final CacheStack<PartCustomization, PartCustomization> customizationStack;
    public final FloatBuffer positions, uvs, normals;

    private static final FiguraVec4 pos = FiguraVec4.of();
    private static final FiguraVec3 normal = FiguraVec3.of();
    private static final FiguraVec3 uv = FiguraVec3.of(0, 0, 1);

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
     * Advances the buffers without drawing those vertices.
     * @param faceCount The number of faces to skip
     */
    public void advanceBuffers(int faceCount) {
        positions.position(positions.position() + faceCount * 12);
        uvs.position(uvs.position() + faceCount * 8);
        normals.position(normals.position() + faceCount * 12);
    }

    public void pushVertices(AvatarRenderer renderer, int faceCount, int[] remainingComplexity) {
        //Handle cases that we can quickly
        if (faceCount == 0)
            return;

        PartCustomization customization = customizationStack.peek();
        if (!customization.visible) {
            advanceBuffers(faceCount);
            //Refund complexity for invisible parts
            remainingComplexity[0] += faceCount;
            return;
        }

        TextureResult primary = this.getTexture(renderer, customization.getPrimaryRenderType(), customization.primaryTexture, textureSet);
        TextureResult secondary = this.getTexture(renderer, customization.getSecondaryRenderType(), customization.secondaryTexture, textureSet);

        if (primary.texture == null && secondary.texture == null) {
            advanceBuffers(faceCount);
            remainingComplexity[0] += faceCount;
            return;
        }

        if (primary.texture != null) {
            if (secondary.texture != null)
                markBuffers();
            pushToConsumer(renderer.bufferSource.getBuffer(primary.texture), faceCount, primary.forceFullbright);
        }
        if (secondary.texture != null) {
            if (primary.texture != null)
                resetBuffers();
            pushToConsumer(renderer.bufferSource.getBuffer(secondary.texture), faceCount, secondary.forceFullbright);
        }
    }

    private record TextureResult(RenderType texture, boolean forceFullbright) {}
    private TextureResult getTexture(AvatarRenderer renderer, RenderTypes types, Pair<FiguraTextureSet.OverrideType, Object> texture, FiguraTextureSet textureSet) {
        if (types == RenderTypes.NONE)
            return new TextureResult(null, false);

        //get texture
        ResourceLocation id = textureSet.getOverrideTexture(renderer.avatar.owner, texture);

        //get render type
        if (id != null) {
            if (renderer.translucent)
                return new TextureResult(RenderType.itemEntityTranslucentCull(id), false);
            if (renderer.glowing)
                return new TextureResult(RenderType.outline(id), false);
        }

        boolean forceFullbright = false;
        if (renderer.doIrisEmissiveFix && types == RenderTypes.EMISSIVE) {
            types = RenderTypes.CUTOUT; //Switch to cutout with fullbright if the iris emissive fix is enabled
            forceFullbright = true;
        }

        return new TextureResult(types == null ? null : types.get(id), forceFullbright);
    }

    private void pushToConsumer(VertexConsumer consumer, int faceCount, boolean forceFullbright) {
        PartCustomization customization = customizationStack.peek();

        FiguraVec3 uvFixer = FiguraVec3.of();
        uvFixer.set(textureSet.getWidth(), textureSet.getHeight(), 1); //Dividing by this makes uv 0 to 1

        for (int i = 0; i < faceCount*4; i++) {

            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.transform(customization.positionMatrix);
            normal.set(normals.get(), normals.get(), normals.get());
            normal.transform(customization.normalMatrix);
            uv.set(uvs.get(), uvs.get(), 1);
            uv.divide(uvFixer);
            uv.transform(customization.uvMatrix);

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

                    customization.overlay,
                    forceFullbright ? LightTexture.FULL_BRIGHT : customization.light,

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
        private final List<Vertex> vertices = new ArrayList<>();

        public static class Vertex {
            public float x, y, z;
            public float u, v;
            public float nx, ny, nz;
        }

        public void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            Vertex vx = new Vertex();
            vx.x = x; vx.y = y; vx.z = z;
            vx.u = u; vx.v = v;
            vx.nx = nx; vx.ny = ny; vx.nz = nz;
            vertices.add(vx);
        }

        public FiguraImmediateBuffer build(FiguraTextureSet textureSet, PartCustomization.Stack customizationStack) {
            int size = vertices.size();
            FloatArrayList positions = new FloatArrayList(size * 3);
            FloatArrayList uvs = new FloatArrayList(size * 2);
            FloatArrayList normals = new FloatArrayList(size * 3);
            for (Vertex vertex : vertices) {
                positions.add(vertex.x);
                positions.add(vertex.y);
                positions.add(vertex.z);
                uvs.add(vertex.u);
                uvs.add(vertex.v);
                normals.add(vertex.nx);
                normals.add(vertex.ny);
                normals.add(vertex.nz);
            }
            return new FiguraImmediateBuffer(positions, uvs, normals, textureSet, customizationStack);
        }
    }
}
