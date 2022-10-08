package org.moon.figura.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.moon.figura.animation.Animation;
import org.moon.figura.animation.Interpolation;
import org.moon.figura.animation.Keyframe;
import org.moon.figura.animation.TransformType;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.model.rendering.FiguraImmediateBuffer;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.model.rendering.texture.RenderTypes;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Take the reading code out of FiguraModelPart itself, since that class
 * was becoming really massive. Reduces bloat slightly
 */
public class FiguraModelPartReader {

    public static FiguraModelPart read(Avatar owner, CompoundTag partCompound, List<FiguraImmediateBuffer.Builder> bufferBuilders, List<FiguraTextureSet> textureSets) {
        //Read name
        String name = partCompound.getString("name");

        //Read transformation
        PartCustomization customization = PartCustomization.of();
        FiguraVec3 target = FiguraVec3.of();
        readVec3(target, partCompound, "rot");
        customization.setRot(target);
        readVec3(target, partCompound, "piv");
        customization.setPivot(target);
        target.free();
        if (partCompound.contains("primary")) {
            try {
                customization.setPrimaryRenderType(RenderTypes.valueOf(partCompound.getString("primary")));
            } catch (Exception ignored) {}
        }
        if (partCompound.contains("secondary")) {
            try {
                customization.setSecondaryRenderType(RenderTypes.valueOf(partCompound.getString("secondary")));
            } catch (Exception ignored) {}
        }
        if (partCompound.contains("vsb"))
            customization.visible = partCompound.getBoolean("vsb");

        customization.needsMatrixRecalculation = true;

        //Read vertex data
        List<Integer> facesByTexture = new ArrayList<>(0);
        if (hasCubeData(partCompound)) {
            readCuboid(facesByTexture, bufferBuilders, partCompound);
            customization.partType = PartCustomization.PartType.CUBE;
        } else if (hasMeshData(partCompound)) {
            //TODO: smooth normals
            readMesh(facesByTexture, bufferBuilders, partCompound);
            customization.partType = PartCustomization.PartType.MESH;
        }

        //Read children
        ArrayList<FiguraModelPart> children = new ArrayList<>(0);
        if (partCompound.contains("chld")) {
            ListTag listTag = partCompound.getList("chld", Tag.TAG_COMPOUND);
            for (Tag tag : listTag)
                children.add(read(owner, (CompoundTag) tag, bufferBuilders, textureSets));
        }

        FiguraModelPart result = new FiguraModelPart(name, customization, children);
        result.facesByTexture = facesByTexture;
        storeTextures(result, textureSets);
        if (partCompound.contains("pt"))
            result.parentType = ParentType.valueOf(partCompound.getString("pt"));

        //Read animations :D
        if (partCompound.contains("anim")) {
            ListTag nbt = partCompound.getList("anim", Tag.TAG_COMPOUND);
            for (Tag tag : nbt) {
                CompoundTag compound = (CompoundTag) tag;
                Animation animation;

                if (!compound.contains("id") || !compound.contains("data") || (animation = owner.animations.get(compound.getInt("id"))) == null)
                    continue;

                CompoundTag animNbt = compound.getCompound("data");
                for (String channelString : animNbt.getAllKeys()) {
                    TransformType type = TransformType.valueOf(channelString.toUpperCase());
                    List<Keyframe> keyframes = new ArrayList<>();
                    ListTag keyframeList = animNbt.getList(channelString, Tag.TAG_COMPOUND);

                    for (Tag keyframeTag : keyframeList) {
                        CompoundTag keyframeNbt = (CompoundTag) keyframeTag;
                        float time = keyframeNbt.getFloat("time");
                        Interpolation interpolation = Interpolation.valueOf(keyframeNbt.getString("int").toUpperCase());

                        FiguraVec3 pos = FiguraVec3.of();
                        readVec3(pos, keyframeNbt, "pre");

                        if (keyframeNbt.contains("end")) {
                            FiguraVec3 end = FiguraVec3.of();
                            readVec3(end, keyframeNbt, "end");
                            keyframes.add(new Keyframe(time, interpolation, pos, end));
                        } else {
                            keyframes.add(new Keyframe(time, interpolation, pos));
                        }
                    }

                    keyframes.sort(Keyframe::compareTo);
                    animation.addAnimation(result, new Animation.AnimationChannel(type, keyframes.toArray(new Keyframe[0])));
                }
            }
        }

        return result;
    }

    /**
     * There's a lot of obscure cases to test this on, so... something might go wrong with it, and I can't test everything.
     * Obviously I *think* it should work, and it has so far, but I still might be missing something.
     */
    private static void storeTextures(FiguraModelPart modelPart, List<FiguraTextureSet> textureSets) {
        //textures
        List<FiguraTextureSet> list = new ArrayList<>();
        for (int j = 0; j < modelPart.facesByTexture.size(); j++)
            list.add(textureSets.get(j));
        modelPart.textures = list;

        //size
        int w = -1, h = -1;
        for (FiguraModelPart child : modelPart.children) {
            //If any child has multiple textures, then we know this parent must as well.
            if (child.textureWidth == -1) {
                modelPart.textureWidth = -1;
                modelPart.textureHeight = -1;
                return;
            }
            //If any child has a texture different than one we've already seen, this parent must have multiple textures.
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
        readVec3(target, tag, name, 0, 0, 0);
    }

    private static void readVec3(FiguraVec3 target, CompoundTag tag, String name, double defX, double defY, double defZ) {
        if (tag.contains(name)) {
            ListTag list = (ListTag) tag.get(name);
            switch (list.getElementType()) {
                case Tag.TAG_FLOAT -> target.set(list.getFloat(0), list.getFloat(1), list.getFloat(2));
                case Tag.TAG_INT -> target.set(list.getInt(0), list.getInt(1), list.getInt(2));
                case Tag.TAG_SHORT -> target.set(list.getShort(0), list.getShort(1), list.getShort(2));
                case Tag.TAG_BYTE -> target.set(
                        ((ByteTag) list.get(0)).getAsByte(),
                        ((ByteTag) list.get(1)).getAsByte(),
                        ((ByteTag) list.get(2)).getAsByte()
                );
            }
        } else {
            target.set(defX, defY, defZ);
        }
    }

    private static void readVec4(FiguraVec4 target, CompoundTag tag, String name) {
        if (tag.contains(name)) {
            ListTag list = (ListTag) tag.get(name);
            switch (list.getElementType()) {
                case Tag.TAG_FLOAT -> target.set(list.getFloat(0), list.getFloat(1), list.getFloat(2), list.getFloat(3));
                case Tag.TAG_INT -> target.set(list.getInt(0), list.getInt(1), list.getInt(2), list.getInt(3));
                case Tag.TAG_SHORT -> target.set(list.getShort(0), list.getShort(1), list.getShort(2), list.getShort(3));
                case Tag.TAG_BYTE -> target.set(
                        ((ByteTag) list.get(0)).getAsByte(),
                        ((ByteTag) list.get(1)).getAsByte(),
                        ((ByteTag) list.get(2)).getAsByte(),
                        ((ByteTag) list.get(3)).getAsByte()
                );
            }
        } else {
            target.set(0, 0, 0, 0);
        }
    }

    private static boolean hasCubeData(CompoundTag partCompound) {
        if (partCompound.contains("cube_data", Tag.TAG_COMPOUND))
            return !partCompound.getCompound("cube_data").isEmpty();
        return false;
    }

    private static boolean hasMeshData(CompoundTag partCompound) {
        if (partCompound.contains("mesh_data", Tag.TAG_COMPOUND))
            return !partCompound.getCompound("mesh_data").isEmpty();
        return false;
    }

    private static final Map<String, FiguraVec3[]> faceData = new ImmutableMap.Builder<String, FiguraVec3[]>()
            .put("n", new FiguraVec3[] {
                    FiguraVec3.of(1, 0, 0),
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(0, 1, 0),
                    FiguraVec3.of(1, 1, 0),
                    FiguraVec3.of(0, 0, -1)
            })
            .put("s", new FiguraVec3[] {
                    FiguraVec3.of(0, 0, 1),
                    FiguraVec3.of(1, 0, 1),
                    FiguraVec3.of(1, 1, 1),
                    FiguraVec3.of(0, 1, 1),
                    FiguraVec3.of(0, 0, 1)
            })
            .put("e", new FiguraVec3[] {
                    FiguraVec3.of(1, 0, 1),
                    FiguraVec3.of(1, 0, 0),
                    FiguraVec3.of(1, 1, 0),
                    FiguraVec3.of(1, 1, 1),
                    FiguraVec3.of(1, 0, 0)
            })
            .put("w", new FiguraVec3[] {
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(0, 0, 1),
                    FiguraVec3.of(0, 1, 1),
                    FiguraVec3.of(0, 1, 0),
                    FiguraVec3.of(-1, 0, 0)
            })
            .put("u", new FiguraVec3[] {
                    FiguraVec3.of(0, 1, 1),
                    FiguraVec3.of(1, 1, 1),
                    FiguraVec3.of(1, 1, 0),
                    FiguraVec3.of(0, 1, 0),
                    FiguraVec3.of(0, 1, 0)
            })
            .put("d", new FiguraVec3[] {
                    FiguraVec3.of(0, 0, 0),
                    FiguraVec3.of(1, 0, 0),
                    FiguraVec3.of(1, 0, 1),
                    FiguraVec3.of(0, 0, 1),
                    FiguraVec3.of(0, -1, 0)
            }).build();

    private static final FiguraVec2[] uvValues = new FiguraVec2[] {
            FiguraVec2.of(0, 1),
            FiguraVec2.of(1, 1),
            FiguraVec2.of(1, 0),
            FiguraVec2.of(0, 0)
    };

    private static final FiguraVec3 from = FiguraVec3.of();
    private static final FiguraVec3 to = FiguraVec3.of();
    private static final FiguraVec3 ftDiff = FiguraVec3.of();

    private static void readCuboid(List<Integer> facesByTexture, List<FiguraImmediateBuffer.Builder> builders, CompoundTag data) {
        //Read from and to
        readVec3(from, data, "f");
        readVec3(to, data, "t");

        //Read inflate
        double inflate = 0;
        if (data.contains("inf"))
            inflate = data.getFloat("inf");
        from.add(-inflate, -inflate, -inflate);
        to.add(inflate, inflate, inflate);

        //Cache difference between from and to
        ftDiff.set(to);
        ftDiff.subtract(from);

        //Iterate over faces, add them
        for (String direction : faceData.keySet())
            readFace(data.getCompound("cube_data"), facesByTexture, builders, direction);
    }

    private static final FiguraVec3 tempPos = FiguraVec3.of();
    private static final FiguraVec4 uv = FiguraVec4.of();

    private static void readFace(CompoundTag faces, List<Integer> facesByTexture, List<FiguraImmediateBuffer.Builder> builders, String direction) {
        if (faces.contains(direction)) {
            CompoundTag face = faces.getCompound(direction);
            short texId = face.getShort("tex");
            while (texId >= facesByTexture.size())
                facesByTexture.add(0);
            while (texId >= builders.size())
                builders.add(FiguraImmediateBuffer.builder());
            facesByTexture.set(texId, facesByTexture.get(texId) + 1);

            FiguraVec3 normal = faceData.get(direction)[4];
            int rotation = (int) (face.getFloat("rot") / 90f);
            readVec4(uv, face, "uv");
            for (int i = 0; i < 4; i++) {
                tempPos.set(ftDiff);
                tempPos.multiply(faceData.get(direction)[i]);
                tempPos.add(from);

                FiguraVec2 normalizedUv = uvValues[(i + rotation)%4];

                builders.get(texId).vertex(
                        (float) tempPos.x, (float) tempPos.y, (float) tempPos.z,
                        (float) Mth.lerp(normalizedUv.x, uv.x, uv.z),
                        (float) Mth.lerp(normalizedUv.y, uv.y, uv.w),
                        (float) normal.x, (float) normal.y, (float) normal.z
                );
            }
        }
    }

    private static void readMesh(List<Integer> facesByTexture, List<FiguraImmediateBuffer.Builder> builders, CompoundTag data) {
        boolean useSmoothShading = false;

        CompoundTag meshData = data.getCompound("mesh_data");
        //mesh_data:
        //"vtx": List<Float>, xyz
        //"tex": List<Short>, (texID << 4) + numVerticesInFace
        //"fac": List<Byte, Short, or Int>, just the indices of various vertices
        //"uvs": List<Float>, uv for each vertex

        if (useSmoothShading)
            readMeshSmooth(facesByTexture, builders, meshData);
        else
            readMeshRegular(facesByTexture, builders, meshData);
    }

    private static final FiguraVec3 p1 = FiguraVec3.of(), p2 = FiguraVec3.of(), p3 = FiguraVec3.of();

    private static void readMeshRegular(List<Integer> facesByTexture, List<FiguraImmediateBuffer.Builder> builders, CompoundTag meshData) {
        ListTag verts = meshData.getList("vtx", Tag.TAG_FLOAT);
        ListTag uvs = meshData.getList("uvs", Tag.TAG_FLOAT);
        ListTag tex = meshData.getList("tex", Tag.TAG_SHORT);

        int bestType = 0; //byte
        if (verts.size() > 255 * 3) bestType = 1; //short
        if (verts.size() > 32767 * 3) bestType = 2; //int

        ListTag fac = switch (bestType) {
            case 0 -> meshData.getList("fac", Tag.TAG_BYTE);
            case 1 -> meshData.getList("fac", Tag.TAG_SHORT);
            default -> meshData.getList("fac", Tag.TAG_INT);
        };

        int vi = 0, uvi = 0;

        float[] posArr = new float[12];
        float[] uvArr = new float[8];

        for (int ti = 0; ti < tex.size(); ti++) {
            short packed = tex.getShort(ti);
            int texId = packed >> 4;
            int numVerts = packed & 0xf;
            while (texId >= facesByTexture.size())
                facesByTexture.add(0);
            while (texId >= builders.size())
                builders.add(FiguraImmediateBuffer.builder());
            facesByTexture.set(texId, facesByTexture.get(texId) + 1);

            for (int j = 0; j < numVerts; j++) {
                int vid = switch (bestType) {
                    case 0 -> ((ByteTag) fac.get(vi + j)).getAsByte() & 0xff;
                    case 1 -> fac.getShort(vi + j) & 0xffff;
                    default -> fac.getInt(vi + j);
                };
                posArr[3*j] = verts.getFloat(3*vid);
                posArr[3*j+1] = verts.getFloat(3*vid+1);
                posArr[3*j+2] = verts.getFloat(3*vid+2);

                uvArr[2*j] = uvs.getFloat(uvi + 2*j);
                uvArr[2*j+1] = uvs.getFloat(uvi + 2*j + 1);
            }

            p1.set(posArr[0], posArr[1], posArr[2]);
            p2.set(posArr[3], posArr[4], posArr[5]);
            p3.set(posArr[6], posArr[7], posArr[8]);
            p3.subtract(p2);
            p1.subtract(p2);
            p3.cross(p1);
            p3.normalize();
            //p3 now contains the normal vector

            for (int j = 0; j < numVerts; j++)
                builders.get(texId).vertex(
                        posArr[3*j], posArr[3*j+1], posArr[3*j+2],
                        uvArr[2*j], uvArr[2*j+1],
                        (float) p3.x, (float) p3.y, (float) p3.z
                );
            if (numVerts == 3)
                builders.get(texId).vertex(
                        posArr[6], posArr[7], posArr[8],
                        uvArr[4], uvArr[5],
                        (float) p3.x, (float) p3.y, (float) p3.z
                );

            vi += numVerts;
            uvi += 2*numVerts;
        }
    }

    private static void readMeshSmooth(List<Integer> facesByTexture, List<FiguraImmediateBuffer.Builder> builders, CompoundTag meshData) {

    }
}
