package org.figuramc.figura.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.util.Mth;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.animation.Interpolation;
import org.figuramc.figura.animation.Keyframe;
import org.figuramc.figura.animation.TransformType;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.figuramc.figura.model.rendering.texture.RenderTypes;
import org.figuramc.figura.parsers.BlockbenchCommonTypes;
import org.figuramc.figura.utils.MathUtils;

import java.util.*;

/**
 * Take the reading code out of FiguraModelPart itself, since that class
 * was becoming really massive. Reduces bloat slightly
 */
public class FiguraModelPartReader {
    public static FiguraModelPart read(
            Avatar owner,
            CompoundTag partCompound,
            List<FiguraTextureSet> textureSets,
            boolean smoothNormals
    ) {
        return read(owner, partCompound, textureSets, smoothNormals, null);
    }

    public static FiguraModelPart read(
            Avatar owner,
            CompoundTag partCompound,
            List<FiguraTextureSet> textureSets,
            boolean smoothNormals,
            Byte inheritedFormatVersion
    ) {
        // if not present, assume v4
        byte formatVersion = partCompound.contains("_v") ? partCompound.getByte("_v") :
                inheritedFormatVersion == null ? BlockbenchCommonTypes.FORMAT_V4 : inheritedFormatVersion;

        // Read name
        String name = partCompound.getStringOr("name", "");

        // Read transformation
        PartCustomization customization = new PartCustomization();
        customization.needsMatrixRecalculation = true;

        FiguraVec3 rot = FiguraVec3.of();
        readVec3(rot, partCompound, "rot");
        customization.setRot(rot);

        FiguraVec3 piv = FiguraVec3.of();
        readVec3(piv, partCompound, "piv");
        customization.setPivot(piv);

        if (partCompound.contains("primary")) {
            try {
                customization.setPrimaryRenderType(RenderTypes.valueOf(partCompound.getStringOr("primary", "")));
            } catch (Exception ignored) {
            }
        }
        if (partCompound.contains("secondary")) {
            try {
                customization.setSecondaryRenderType(RenderTypes.valueOf(partCompound.getStringOr("secondary", "")));
            } catch (Exception ignored) {
            }
        }

        if (partCompound.contains("vsb"))
            customization.visible = partCompound.getBooleanOr("vsb", false);

        // textures
        List<Integer> facesByTexture = new ArrayList<>(0);
        while (textureSets.size() > facesByTexture.size())
            facesByTexture.add(0);

        // Read vertex data
        Map<Integer, List<Vertex>> vertices = new HashMap<>();
        if (hasCubeData(partCompound)) {
            readCuboid(facesByTexture, partCompound, vertices);
            customization.partType = PartCustomization.PartType.CUBE;
        } else if (hasMeshData(partCompound)) {
            readMesh(facesByTexture, partCompound, vertices);
            customization.partType = PartCustomization.PartType.MESH;
        }

        // smooth normals
        if (partCompound.contains("smo"))
            smoothNormals = partCompound.getBooleanOr("smo", false);

        if (Configs.FORCE_SMOOTH_AVATAR.value || (smoothNormals && !vertices.isEmpty()))
            smoothfy(vertices);

        // Read children
        ArrayList<FiguraModelPart> children = new ArrayList<>(0);
        if (partCompound.contains("chld")) {
            ListTag listTag = partCompound.getListOrEmpty("chld");
            for (Tag tag : listTag)
                children.add(read(owner, (CompoundTag) tag, textureSets, smoothNormals, formatVersion));
        }

        FiguraModelPart result = new FiguraModelPart(
                owner, name, customization, vertices,
                children, formatVersion
        );

        for (FiguraModelPart child : children)
            child.parent = result;

        result.facesByTexture = facesByTexture;
        storeTextures(result, textureSets);
        if (partCompound.contains("pt")) {
            try {
                result.parentType = ParentType.valueOf(partCompound.getStringOr("pt", ""));
            } catch (Exception ignored) {
            }
        }

        // Read animations :D
        if (partCompound.contains("anim")) {
            ListTag nbt = partCompound.getListOrEmpty("anim");
            for (Tag tag : nbt) {
                CompoundTag compound = (CompoundTag) tag;
                Animation animation;

                if (!compound.contains("id") || !compound.contains("data") || (animation = owner.animations.get(compound.getIntOr(
                        "id", 0))) == null)
                    continue;

                CompoundTag animNbt = compound.getCompoundOrEmpty("data");
                for (String channelString : animNbt.keySet()) {
                    TransformType type = switch (channelString) {
                        case "pos" -> TransformType.POSITION;
                        case "rot" -> TransformType.ROTATION;
                        case "grot" -> TransformType.GLOBAL_ROT;
                        case "scl" -> TransformType.SCALE;
                        default -> null;
                    };

                    if (type == null)
                        continue;

                    List<Keyframe> keyframes = new ArrayList<>();
                    ListTag keyframeList = animNbt.getListOrEmpty(channelString);

                    for (Tag keyframeTag : keyframeList) {
                        CompoundTag keyframeNbt = (CompoundTag) keyframeTag;
                        float time = keyframeNbt.getFloatOr("time", 0);
                        Interpolation interpolation;
                        try {
                            interpolation = Interpolation.valueOf(keyframeNbt.getStringOr("int", "").toUpperCase(Locale.US));
                        } catch (Exception e) {
                            FiguraMod.LOGGER.error(
                                    "Invalid interpolation type in the model {}, something is wrong with this model!",
                                    keyframeNbt.getString("int")
                            );
                            FiguraMod.LOGGER.error("", e);
                            continue;
                        }

                        Pair<FiguraVec3, String[]> pre = parseKeyframeData(keyframeNbt, "pre");
                        if (pre == null) pre = Pair.of(FiguraVec3.of(), null);
                        Pair<FiguraVec3, String[]> end = parseKeyframeData(keyframeNbt, "end");
                        if (end == null) end = pre;

                        FiguraVec3 bezierLeft = FiguraVec3.of();
                        FiguraVec3 bezierRight = FiguraVec3.of();
                        readVec3(bezierLeft, keyframeNbt, "bl");
                        readVec3(bezierRight, keyframeNbt, "br");

                        FiguraVec3 bezierLeftTime = FiguraVec3.of(-0.1, -0.1, -0.1);
                        FiguraVec3 bezierRightTime = FiguraVec3.of(0.1, 0.1, 0.1);
                        readVec3(bezierLeftTime, keyframeNbt, "blt");
                        readVec3(bezierRightTime, keyframeNbt, "brt");
                        bezierLeftTime.add(1, 1, 1);
                        bezierLeftTime = MathUtils.clamp(bezierLeftTime, 0, 1);
                        bezierRightTime = MathUtils.clamp(bezierRightTime, 0, 1);

                        keyframes.add(new Keyframe(
                                owner,
                                result,
                                type,
                                animation,
                                time,
                                interpolation,
                                pre,
                                end,
                                bezierLeft,
                                bezierRight,
                                bezierLeftTime,
                                bezierRightTime
                        ));
                    }

                    keyframes.sort(Keyframe::compareTo);
                    animation.addAnimation(
                            result,
                            new Animation.AnimationChannel(type, keyframes.toArray(new Keyframe[0]))
                    );
                }
            }
        }

        return result;
    }

    private static Pair<FiguraVec3, String[]> parseKeyframeData(CompoundTag keyframeNbt, String tag) {
        if (!keyframeNbt.contains(tag))
            return null;

        ListTag floatList = keyframeNbt.getListOrEmpty(tag);
        if (!floatList.isEmpty()) {
            FiguraVec3 ret = FiguraVec3.of();
            readVec3(ret, floatList);
            return Pair.of(ret, null);
        } else {
            ListTag stringList = keyframeNbt.getListOrEmpty(tag);
            return Pair.of(
                    null,
                    new String[]{stringList.getStringOr(0, ""), stringList.getStringOr(1, ""), stringList.getStringOr(2, "")}
            );
        }
    }

    /**
     * There's a lot of obscure cases to test this on, so... something might go wrong with it, and I can't test everything.
     * Obviously I *think* it should work, and it has so far, but I still might be missing something.
     */
    private static void storeTextures(FiguraModelPart modelPart, List<FiguraTextureSet> textureSets) {
        // textures
        List<FiguraTextureSet> list = new ArrayList<>(0);
        for (int j = 0; j < modelPart.facesByTexture.size(); j++)
            list.add(textureSets.get(j));
        modelPart.textures = list;

        // size
        int w = -1, h = -1;
        for (FiguraModelPart child : modelPart.children) {
            // If any child has multiple textures, then we know this parent must as well.
            if (child.textureWidth == -1) {
                modelPart.textureWidth = -1;
                modelPart.textureHeight = -1;
                return;
            }
            // If any child has a texture different than one we've already seen, this parent must have multiple textures.
            if (child.textureWidth != w || child.textureHeight != h) {
                if (w != -1) {
                    modelPart.textureWidth = -1;
                    modelPart.textureHeight = -1;
                    return;
                }
                w = child.textureWidth;
                h = child.textureHeight;
            }
        }
        if (modelPart.customization.partType != PartCustomization.PartType.GROUP) {
            int i = -1;
            for (int j = 0; j < modelPart.facesByTexture.size(); j++) {
                if (modelPart.facesByTexture.get(j) > 0) {
                    int realTexWidth = textureSets.get(j).getWidth();
                    int realTexHeight = textureSets.get(j).getHeight();
                    if ((w != -1 && w != realTexWidth) || (h != -1 && h != realTexHeight)) {
                        modelPart.textureWidth = -1;
                        modelPart.textureHeight = -1;
                        return;
                    }
                    if (i != -1) {
                        modelPart.textureWidth = -1;
                        modelPart.textureHeight = -1;
                        return;
                    }
                    i = j;
                    w = realTexWidth;
                    h = realTexHeight;
                }
            }
        }
        modelPart.textureWidth = w;
        modelPart.textureHeight = h;
    }

    private static void readVec3(FiguraVec3 target, CompoundTag tag, String name) {
        if (tag.contains(name))
            readVec3(target, (ListTag) tag.get(name));
    }

    private static void readVec3(FiguraVec3 target, ListTag list) {
        switch (list.getFirst().getId()) {
            case Tag.TAG_FLOAT -> target.set((Object) list.getFloatOr(0, 0.0f), list.getFloatOr(1, 0.0f), list.getFloatOr(2, 0.0f));
            case Tag.TAG_INT -> target.set((Object) list.getIntOr(0, 0), list.getIntOr(1, 0), list.getIntOr(2, 0));
            case Tag.TAG_SHORT -> target.set((Object) list.getShortOr(0, (short) 0), list.getShortOr(1, (short) 0), list.getShortOr(2, (short) 0));
            case Tag.TAG_BYTE -> target.set(
                    ((ByteTag) list.get(0)).byteValue(),
                    ((ByteTag) list.get(1)).byteValue(),
                    ((ByteTag) list.get(2)).byteValue()
            );
        }
    }

    private static void readVec4(FiguraVec4 target, CompoundTag tag, String name) {
        if (tag.contains(name)) {
            ListTag list = (ListTag) tag.get(name);
            switch (list.getFirst().getId()) {
                case Tag.TAG_FLOAT ->
                        target.set((Object) list.getFloatOr(0, 0.0f), list.getFloatOr(1, 0.0f), list.getFloatOr(2, 0.0f), list.getFloatOr(3, 0.0f));
                case Tag.TAG_INT -> target.set((Object) list.getIntOr(0, 0), list.getIntOr(1, 0), list.getIntOr(2, 0), list.getIntOr(3, 0));
                case Tag.TAG_SHORT ->
                        target.set((Object) list.getShortOr(0, (short) 0), list.getShortOr(1, (short) 0), list.getShortOr(2, (short) 0), list.getShortOr(3, (short) 0));
                case Tag.TAG_BYTE -> target.set(
                        ((ByteTag) list.get(0)).byteValue(),
                        ((ByteTag) list.get(1)).byteValue(),
                        ((ByteTag) list.get(2)).byteValue(),
                        ((ByteTag) list.get(3)).byteValue()
                );
            }
        } else {
            target.set(0, 0, 0, 0);
        }
    }

    private static boolean hasCubeData(CompoundTag partCompound) {
        if (partCompound.contains("cube_data") && partCompound.getCompound("cube_data").isPresent())
            return !partCompound.getCompound("cube_data").get().isEmpty();
        return false;
    }

    private static boolean hasMeshData(CompoundTag partCompound) {
        if (partCompound.contains("mesh_data") && partCompound.getCompound("mesh_data").isPresent())
            return !partCompound.getCompound("mesh_data").get().isEmpty();
        return false;
    }

    private static final Map<String, FiguraVec3[]> faceData = ImmutableMap.of( // booze 🥴
            "n", new FiguraVec3[]{
                    FiguraVec3.of(1, 0, 0),
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(0, 1, 0),
                    FiguraVec3.of(1, 1, 0),
                    FiguraVec3.of(0, 0, -1)
            },
            "s", new FiguraVec3[]{
                    FiguraVec3.of(0, 0, 1),
                    FiguraVec3.of(1, 0, 1),
                    FiguraVec3.of(1, 1, 1),
                    FiguraVec3.of(0, 1, 1),
                    FiguraVec3.of(0, 0, 1)
            },
            "e", new FiguraVec3[]{
                    FiguraVec3.of(1, 0, 1),
                    FiguraVec3.of(1, 0, 0),
                    FiguraVec3.of(1, 1, 0),
                    FiguraVec3.of(1, 1, 1),
                    FiguraVec3.of(1, 0, 0)
            },
            "w", new FiguraVec3[]{
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(0, 0, 1),
                    FiguraVec3.of(0, 1, 1),
                    FiguraVec3.of(0, 1, 0),
                    FiguraVec3.of(-1, 0, 0)
            },
            "u", new FiguraVec3[]{
                    FiguraVec3.of(0, 1, 1),
                    FiguraVec3.of(1, 1, 1),
                    FiguraVec3.of(1, 1, 0),
                    FiguraVec3.of(0, 1, 0),
                    FiguraVec3.of(0, 1, 0)
            },
            "d", new FiguraVec3[]{
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(1, 0, 0),
                    FiguraVec3.of(1, 0, 1),
                    FiguraVec3.of(0, 0, 1),
                    FiguraVec3.of(0, -1, 0)
            }
    );

    private static final FiguraVec2[] uvValues = {
            FiguraVec2.of(0, 1),
            FiguraVec2.of(1, 1),
            FiguraVec2.of(1, 0),
            FiguraVec2.of(0, 0)
    };


    private static void readCuboid(List<Integer> facesByTexture,
                                   CompoundTag data,
                                   Map<Integer, List<Vertex>> vertices) {
        // Read from and to
        FiguraVec3 from = FiguraVec3.of();
        readVec3(from, data, "f");
        FiguraVec3 to = FiguraVec3.of();
        readVec3(to, data, "t");

        // Read inflate
        double inflate = 0;
        if (data.contains("inf"))
            inflate = data.getFloatOr("inf", 0.0f);
        from.add(-inflate, -inflate, -inflate);
        to.add(inflate, inflate, inflate);

        // Cache difference between from and to
        FiguraVec3 ftDiff = to.copy();
        ftDiff.subtract(from);

        // Iterate over faces, add them
        for (String direction : faceData.keySet())
            readFace(data.getCompoundOrEmpty("cube_data"), facesByTexture, direction, vertices, from, ftDiff);
    }

    private static void readFace(CompoundTag faces,
                                 List<Integer> facesByTexture,
                                 String direction,
                                 Map<Integer, List<Vertex>> vertices,
                                 FiguraVec3 from,
                                 FiguraVec3 ftDiff) {
        if (faces.contains(direction)) {
            CompoundTag face = faces.getCompoundOrEmpty(direction);
            short texId = face.getShortOr("tex", (short) 0);
            facesByTexture.set(texId, facesByTexture.get(texId) + 1);

            FiguraVec3 normal = faceData.get(direction)[4];
            int rotation = (int) (face.getFloatOr("rot", 0.0f) / 90f);
            FiguraVec4 uv = FiguraVec4.of();
            readVec4(uv, face, "uv");
            for (int i = 0; i < 4; i++) {
                FiguraVec3 tempPos = ftDiff.copy();
                tempPos.multiply(faceData.get(direction)[i]);
                tempPos.add(from);

                FiguraVec2 normalizedUv = uvValues[(i + rotation) % 4];

                List<Vertex> list = vertices.getOrDefault((int) texId, new ArrayList<>());
                list.add(new Vertex(
                        (float) tempPos.x, (float) tempPos.y, (float) tempPos.z,
                        (float) Mth.lerp(normalizedUv.x, uv.x, uv.z),
                        (float) Mth.lerp(normalizedUv.y, uv.y, uv.w),
                        (float) normal.x, (float) normal.y, (float) normal.z
                ));
                vertices.put((int) texId, list);
            }
        }
    }

    private static void readMesh(List<Integer> facesByTexture, CompoundTag data, Map<Integer, List<Vertex>> vertices) {
        CompoundTag meshData = data.getCompoundOrEmpty("mesh_data");
        // mesh_data:
        // "vtx": List<Float>, xyz
        // "tex": List<Short>, (texID << 4) + numVerticesInFace
        // "fac": List<Byte, Short, or Int>, just the indices of various vertices
        // "uvs": List<Float>, uv for each vertex

        // Get the vertex, UV, and texture lists from the mesh data
        ListTag verts = meshData.getListOrEmpty("vtx");
        ListTag uvs = meshData.getListOrEmpty("uvs");
        ListTag tex = meshData.getListOrEmpty("tex");

        // Determine the best data type to use for the face list based on the size of the vertex list
        int bestType = 0; // byte
        if (verts.size() > 255 * 3) bestType = 1; // short
        if (verts.size() > 32767 * 3) bestType = 2; // int

        // Get the face list using the determined data type
        ListTag fac = meshData.getListOrEmpty("fac");

        // Initialize counters for the vertex and UV lists
        int vi = 0, uvi = 0;

        // Create arrays to store temporary vertex and UV data
        float[] posArr = new float[12];
        float[] uvArr = new float[8];

        // Iterate through the texture list
        for (int ti = 0; ti < tex.size(); ti++) {
            // Get the packed texture data for this iteration
            short packed = tex.getShortOr(ti, (short) 0);
            // Extract the texture ID and number of vertices from the packed data
            int texId = packed >> 4;
            int numVerts = packed & 0xf;
            // Increment the number of faces for the current texture ID
            facesByTexture.set(texId, facesByTexture.get(texId) + 1);

            // Extract the vertex and UV data for the current texture
            for (int j = 0; j < numVerts; j++) {
                // Get the vertex ID based on the determined data type
                int vid = switch (bestType) {
                    case 0 -> ((ByteTag) fac.get(vi + j)).byteValue() & 0xff;
                    case 1 -> fac.getShortOr(vi + j, (short) 0) & 0xffff;
                    default -> fac.getIntOr(vi + j, 0);
                };
                // Get the vertex position and UV data from the lists
                posArr[3 * j] = verts.getFloatOr(3 * vid, 0.0f);
                posArr[3 * j + 1] = verts.getFloatOr(3 * vid + 1, 0.0f);
                posArr[3 * j + 2] = verts.getFloatOr(3 * vid + 2, 0.0f);

                uvArr[2 * j] = uvs.getFloatOr(uvi + 2 * j, 0.0f);
                uvArr[2 * j + 1] = uvs.getFloatOr(uvi + 2 * j + 1, 0.0f);
            }

            // Calculate the normal vector for the current texture
            FiguraVec3 p1 = FiguraVec3.of(posArr[0], posArr[1], posArr[2]);
            FiguraVec3 p2 = FiguraVec3.of(posArr[3], posArr[4], posArr[5]);
            FiguraVec3 p3 = FiguraVec3.of(posArr[6], posArr[7], posArr[8]);
            p3.subtract(p2);
            p1.subtract(p2);
            p3.cross(p1);
            p3.normalize();
            // p3 now contains the normal vector

            // Add the vertex data to the appropriate builder
            for (int j = 0; j < numVerts; j++) {
                List<Vertex> list = vertices.getOrDefault(texId, new ArrayList<>());
                list.add(new Vertex(
                        posArr[3 * j], posArr[3 * j + 1], posArr[3 * j + 2],
                        uvArr[2 * j], uvArr[2 * j + 1],
                        (float) p3.x, (float) p3.y, (float) p3.z
                ));
                vertices.put(texId, list);
            }
            // Add a vertex if necessary
            if (numVerts == 3) {
                List<Vertex> list = vertices.getOrDefault(texId, new ArrayList<>());
                list.add(new Vertex(
                        posArr[6], posArr[7], posArr[8],
                        uvArr[4], uvArr[5],
                        (float) p3.x, (float) p3.y, (float) p3.z
                ));
                vertices.put(texId, list);
            }

            // Increment the counters for the vertex and UV lists
            vi += numVerts;
            uvi += 2 * numVerts;
        }
    }

    // thanks to Scarlet Light#7611
    private static void smoothfy(Map<Integer, List<Vertex>> verticesByTextuers) {
        // separate vertices
        Map<String, List<Vertex>> verticesByPos = new HashMap<>();
        for (List<Vertex> vertices : verticesByTextuers.values()) {
            for (Vertex vertex : vertices) {
                String id = String.valueOf(vertex.getPos());
                List<Vertex> list = verticesByPos.computeIfAbsent(id, str -> new ArrayList<>(4));
                list.add(vertex);
            }
        }

        // for all separated vertices
        for (List<Vertex> vertices : verticesByPos.values()) {
            // sum their normals
            FiguraVec3 result = FiguraVec3.of();
            for (Vertex vertex : vertices)
                result.add(vertex.getNormal());
            // normalize the normal
            result.normalize();
            // apply new normal
            for (Vertex vertex : vertices)
                vertex.setNormal(result);
        }
    }
}
