package org.moon.figura.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.rendering.FiguraImmediateBuffer;
import org.moon.figura.model.rendering.ImmediateAvatarRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FiguraModelPart {

    public final String name;
    public final ModelPartTransform transform;
    public final int index;
    public final List<FiguraModelPart> children;

    private List<Integer> facesByTexture;

    public void pushVerticesImmediate(ImmediateAvatarRenderer avatarRenderer) {
        for (int i = 0; i < facesByTexture.size(); i++)
            avatarRenderer.pushFaces(i, facesByTexture.get(i));
    }


    //-- READING METHODS FROM NBT --//

    private FiguraModelPart(String name, ModelPartTransform transform, int index, List<FiguraModelPart> children) {
        this.name = name;
        this.transform = transform;
        this.index = index;
        this.children = children;
    }

    public static FiguraModelPart read(CompoundTag partCompound, FiguraImmediateBuffer.Builder vertexBuffer) {
        return read(partCompound, vertexBuffer, new int[] {0});
    }

    private static FiguraModelPart read(CompoundTag partCompound, FiguraImmediateBuffer.Builder vertexBuffer, int[] index) {
        //Read name
        String name = partCompound.getString("name");

        //Read transformation
        ModelPartTransform transform = new ModelPartTransform();
        readVec3(transform.rotation, partCompound, "rot");
        readVec3(transform.pivot, partCompound, "piv");
        transform.needsMatrixRecalculation = true;

        //Read vertex data
        int newIndex = -1;
        List<Integer> facesByTexture = new ArrayList<>(0);
        if (hasCubeData(partCompound)) {
            readCuboid(facesByTexture, vertexBuffer, partCompound);
            newIndex = index[0]++;
        } else if (hasMeshData(partCompound)) {
            //TODO: read mesh
            newIndex = index[0]++;
        }

        //Read children
        ArrayList<FiguraModelPart> children = new ArrayList<>(0);
        if (partCompound.contains("chld")) {
            ListTag listTag = partCompound.getList("chld", Tag.TAG_COMPOUND);
            for (Tag tag : listTag)
                children.add(read((CompoundTag) tag, vertexBuffer, index));
        }

        FiguraModelPart result = new FiguraModelPart(name, transform, newIndex, children);
        result.facesByTexture = facesByTexture;
        return result;
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

    private static void readCuboid(List<Integer> facesByTexture, FiguraImmediateBuffer.Builder builder, CompoundTag data) {
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
            readFace(data.getCompound("cube_data"), facesByTexture, builder, direction);
    }

    private static final FiguraVec3 tempPos = FiguraVec3.of();
    private static final FiguraVec4 uv = FiguraVec4.of();

    private static void readFace(CompoundTag faces, List<Integer> facesByTexture, FiguraImmediateBuffer.Builder builder, String direction) {
        if (faces.contains(direction)) {
            CompoundTag face = faces.getCompound(direction);
            short texId = face.getShort("tex");
            while (texId >= facesByTexture.size())
                facesByTexture.add(0);
            facesByTexture.set(texId, facesByTexture.get(texId) + 1);

            FiguraVec3 normal = faceData.get(direction)[4];
            int rotation = (int) (face.getFloat("rot") / 90f);
            readVec4(uv, face, "uv");
            for (int i = 0; i < 4; i++) {
                tempPos.set(ftDiff);
                tempPos.multiply(faceData.get(direction)[i]);
                tempPos.add(from);

                FiguraVec2 normalizedUv = uvValues[(i + rotation)%4];

                builder.vertex(
                        (float) tempPos.x, (float) tempPos.y, (float) tempPos.z,
                        (float) Mth.lerp(normalizedUv.x, uv.x, uv.z),
                        (float) Mth.lerp(normalizedUv.y, uv.y, uv.w),
                        (float) normal.x, (float) normal.y, (float) normal.z
                );
            }
        }
    }

}
