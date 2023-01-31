package org.moon.figura.model.rendering;

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
import java.util.LinkedHashMap;
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

    public void pushVertices(ImmediateAvatarRenderer renderer, int faceCount, int[] remainingComplexity) {
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

        VertexData primary = this.getTexture(renderer, customization, textureSet, true);
        VertexData secondary = this.getTexture(renderer, customization, textureSet, false);

        if (primary.renderType == null && secondary.renderType == null) {
            advanceBuffers(faceCount);
            remainingComplexity[0] += faceCount;
            return;
        }

        if (primary.renderType != null) {
            if (secondary.renderType != null)
                markBuffers();
            pushToBuffer(faceCount, primary);
        }
        if (secondary.renderType != null) {
            if (primary.renderType != null)
                resetBuffers();
            pushToBuffer(faceCount, secondary);
        }
    }

    private VertexData getTexture(ImmediateAvatarRenderer renderer, PartCustomization customization, FiguraTextureSet textureSet, boolean primary) {
        RenderTypes types = primary ? customization.getPrimaryRenderType() : customization.getSecondaryRenderType();
        Pair<FiguraTextureSet.OverrideType, Object> texture = primary ? customization.primaryTexture : customization.secondaryTexture;

        if (types == RenderTypes.NONE)
            return new VertexData();

        //get texture
        ResourceLocation id = textureSet.getOverrideTexture(renderer.avatar.owner, texture);

        //get render type
        if (id != null) {
            if (renderer.translucent)
                return new VertexData(RenderTypes.TRANSLUCENT_CULL, RenderType.itemEntityTranslucentCull(id));
            if (renderer.glowing)
                return new VertexData(RenderTypes.TRANSLUCENT_CULL, RenderType.outline(id));
        }

        if (types == null)
            return new VertexData();

        RenderType renderType;
        boolean fullBright = false;
        boolean offset = renderer.offsetRenderLayers && !primary && types.isOffset();
        //Switch to cutout with fullbright if the iris emissive fix is enabled
        if (renderer.doIrisEmissiveFix && types == RenderTypes.EMISSIVE) {
            fullBright = true;
            renderType = RenderTypes.CUTOUT.get(id);
        } else {
            renderType = types.get(id);
        }

        return new VertexData(types, renderType, offset, fullBright);
    }

    private void pushToBuffer(int faceCount, VertexData vertexData) {
        LinkedHashMap<RenderType, FloatArrayList> bufferMap = ImmediateAvatarRenderer.VERTICES.computeIfAbsent(vertexData.type, renderTypes -> new LinkedHashMap<>());
        FloatArrayList buffer = bufferMap.computeIfAbsent(vertexData.renderType, renderType -> new FloatArrayList());

        PartCustomization customization = customizationStack.peek();

        FiguraVec3 uvFixer = FiguraVec3.of();
        uvFixer.set(textureSet.getWidth(), textureSet.getHeight(), 1); //Dividing by this makes uv 0 to 1

        double light = vertexData.fullBright ? LightTexture.FULL_BRIGHT : customization.light;
        double vertexOffset = vertexData.offsetVertices ? 0.0001f : 0f;

        for (int i = 0; i < faceCount * 4; i++) {
            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.transform(customization.positionMatrix);
            normal.set(normals.get(), normals.get(), normals.get());
            normal.transform(customization.normalMatrix);
            uv.set(uvs.get(), uvs.get(), 1);
            uv.divide(uvFixer);
            uv.transform(customization.uvMatrix);

            buffer.add((float) pos.x);
            buffer.add((float) pos.y);
            buffer.add((float) (pos.z + vertexOffset));

            buffer.add((float) customization.color.x);
            buffer.add((float) customization.color.y);
            buffer.add((float) customization.color.z);
            buffer.add((float) customization.alpha);

            buffer.add((float) uv.x);
            buffer.add((float) uv.y);

            buffer.add((float) customization.overlay);
            buffer.add((float) light);

            buffer.add((float) normal.x);
            buffer.add((float) normal.y);
            buffer.add((float) normal.z);
        }

        uvFixer.free();
    }

    public static class VertexData {
        public final RenderTypes type;
        public final RenderType renderType;
        public final boolean fullBright;
        public final boolean offsetVertices;

        public VertexData() {
            this(null, null);
        }

        public VertexData(RenderTypes type, RenderType renderType) {
            this(type, renderType, false);
        }

        public VertexData(RenderTypes type, RenderType renderType, boolean offsetVertices) {
            this(type, renderType, offsetVertices, false);
        }

        public VertexData(RenderTypes type, RenderType renderType, boolean offsetVertices, boolean fullBright) {
            this.type = type;
            this.renderType = renderType;
            this.offsetVertices = offsetVertices;
            this.fullBright = fullBright;
        }
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
