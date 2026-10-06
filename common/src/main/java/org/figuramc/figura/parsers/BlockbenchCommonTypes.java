package org.figuramc.figura.parsers;

import com.google.gson.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.ParentType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class BlockbenchCommonTypes {
    public static final byte FORMATLESS = 0x00;
    public static final byte FORMAT_V4 = 0x01;
    public static final byte FORMAT_V5 = 0x02;
    private static final FiguraVec3 ZERO = FiguraVec3.of(0, 0, 0);
    private static final FiguraVec4 ZERO4 = FiguraVec4.of(0, 0, 0, 0);

    public static class IntPair {
        public int x;
        public int y;

        public IntPair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(BlockbenchV5Model.class, BlockbenchV5Model.MODEL_DESERIALIZER)
                .registerTypeAdapter(BlockbenchV4Model.class, BlockbenchV4Model.MODEL_DESERIALIZER)
                .registerTypeAdapter(FiguraVec4.class, VEC4_DESERIALIZER)
                .registerTypeAdapter(FiguraVec3.class, VEC3_DESERIALIZER)
                .registerTypeAdapter(FiguraVec2.class, VEC2_DESERIALIZER)
                .registerTypeAdapter(BlockbenchV5Model.OutlinerItem.class, BlockbenchV5Model.OutlinerItem.DESERIALIZER)
                .registerTypeAdapter(
                        BlockbenchV4Model.OutlinerItem.Element.class,
                        BlockbenchV4Model.OutlinerItem.Element.DESERIALIZER
                )
                .registerTypeAdapterFactory(ModelFormat.ADAPTER_FACTORY)
                .registerTypeAdapterFactory(Element.ADAPTER_FACTORY)
                .registerTypeAdapterFactory(Keyframe.ADAPTER_FACTORY)
                .registerTypeAdapterFactory(BlockbenchV4Model.OutlinerItem.ADAPTER_FACTORY)
                .create();
    }

    public static void parseParent(String name, CompoundTag nbt) {
        ParentType parentType = ParentType.get(name);
        if (parentType != ParentType.None)
            nbt.putString("pt", parentType.name());
    }

    public static IntPair getPNGDimensions(byte[] png) {
        // this grabs the two values from the header ._.
        // duplicate of BlockbenchModelParser.getTextureSize
        int w = (int) png[16] & 0xFF;
        w = (w << 8) + ((int) png[17] & 0xFF);
        w = (w << 8) + ((int) png[18] & 0xFF);
        w = (w << 8) + ((int) png[19] & 0xFF);

        int h = (int) png[20] & 0xFF;
        h = (h << 8) + ((int) png[21] & 0xFF);
        h = (h << 8) + ((int) png[22] & 0xFF);
        h = (h << 8) + ((int) png[23] & 0xFF);

        return new IntPair(w, h);
    }

    public static void attachCollections(BlockbenchParser2.Intermediary context, String uuid, CompoundTag target) {
        List<Integer> collections = new ArrayList<>(context.collectionsByElement.get(uuid));
        if (!collections.isEmpty()) {
            byte[] prs = new byte[collections.size()];
            for (int i = 0; i < collections.size(); i++) prs[i] = collections.get(i).byteValue();
            target.put("pr", new ByteArrayTag(prs));
        }
    }

    public static abstract class ModelFormat {
        String formatVersion;

        public static final Pattern FORMAT_VERSION_MATCHER = Pattern.compile("^(\\d+?)\\.");

        public static final TypeAdapterFactory ADAPTER_FACTORY = new GsonTypeByField<>(
                ModelFormat.class, obj -> {
            String version = obj.getAsJsonObject().getAsJsonObject("meta").get("format_version").getAsString();
            Matcher matcher = FORMAT_VERSION_MATCHER.matcher(version);
            if (!matcher.find()) throw new IllegalStateException("No match on version number");
            return matcher.group(1);
        }
        ).bind("5", BlockbenchV5Model.class).bind("4", BlockbenchV4Model.class);
        String modelFormat;

        public abstract Map<String, UUIDReferable> getAllReferences();

        public abstract CompoundTag convert(BlockbenchParser2.Intermediary target);
    }

    private static float floatOrZero(JsonElement it) {
        if (!it.isJsonPrimitive()) return 0f;
        JsonPrimitive prim = it.getAsJsonPrimitive();
        if (!prim.isNumber()) return 0f;
        return prim.getAsFloat();
    }

    public static final JsonDeserializer<FiguraVec4> VEC4_DESERIALIZER = (json, typeOfT, context) -> {
        JsonArray four = json.getAsJsonArray();
        if (four.size() != 4)
            throw new JsonParseException("Trying to parse a Vector4, but the list has " + four.size() + " items (expected 4)");
        return FiguraVec4.of(
                floatOrZero(four.get(0)),
                floatOrZero(four.get(1)),
                floatOrZero(four.get(2)),
                floatOrZero(four.get(3))
        );
    };
    public static final JsonDeserializer<FiguraVec3> VEC3_DESERIALIZER = (json, typeOfT, context) -> {
        JsonArray three = json.getAsJsonArray();
        if (three.size() != 3)
            throw new JsonParseException("Trying to parse a Vector3, but the list has " + three.size() + " items (expected 3)");
        return FiguraVec3.of(
                floatOrZero(three.get(0)),
                floatOrZero(three.get(1)),
                floatOrZero(three.get(2))
        );
    };
    public static final JsonDeserializer<FiguraVec2> VEC2_DESERIALIZER = (json, typeOfT, context) -> {
        JsonArray two = json.getAsJsonArray();
        if (two.size() != 2)
            throw new JsonParseException("Trying to parse a Vector2, but the list has " + two.size() + " items (expected 2)");
        return FiguraVec2.of(
                floatOrZero(two.get(0)),
                floatOrZero(two.get(1))
        );
    };

    public static ListTag floatArrToList(float[] fa) {
        int type = 0;
        for (float c : fa) {
            if (c % 1 == 0) {
                // short?
                if (c < -127 || c >= 128) type = 1;
                // full float?
                if (c < -16383 || c >= 16384) {
                    type = 2;
                    break;
                }
            } else {
                type = 2;
                break;
            }
        }

        ListTag tag = new ListTag();

        for (float c : fa) {
            switch (type) {
                case 0 -> tag.add(ByteTag.valueOf((byte) c));
                case 1 -> tag.add(ShortTag.valueOf((short) c));
                case 2 -> tag.add(FloatTag.valueOf(c));
            }
        }
        return tag;
    }

    public static ListTag vecToList(FiguraVec3 vector) {
        return floatArrToList(new float[]{(float) vector.x, (float) vector.y, (float) vector.z});
    }

    public static ListTag vecToList(FiguraVec4 vector) {
        return floatArrToList(new float[]{(float) vector.x, (float) vector.y, (float) vector.z, (float) vector.w});
    }

    public static float parseFloatOr(@Nullable String floatLike, float fallback) {
        // fail-fast the most common not-numbers
        if (floatLike == null || floatLike.isBlank()) return fallback;
        try {
            return Float.parseFloat(floatLike);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public interface UUIDReferable {
        String getUUID();
    }

    public interface NBTRepresentation<T extends Tag> {
        T toNBT(BlockbenchParser2.Intermediary context);
    }

    @SuppressWarnings("unused")
    public static abstract class Element implements UUIDReferable, NBTRepresentation<CompoundTag> {
        public static final TypeAdapterFactory ADAPTER_FACTORY =
                new GsonTypeByField<>(Element.class, "type")
                        .bind("cube", CubeElement.class)
                        .bind("mesh", MeshElement.class)
                        .bind("locator", PointElement.class)
                        .bind("null_object", PointElement.class)
                        .withFallback(UnknownElement.class);

        String name;
        String type;
        String uuid;

        FiguraVec3 rotation;
        FiguraVec3 origin;

        @Nullable Boolean visibility;
        @Nullable Boolean export;

        @Override
        public String getUUID() {
            return uuid;
        }

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = new CompoundTag();

            if (Boolean.FALSE.equals(export)) return null;

            tag.putString("name", name);
            if (context.options.partsWithUUIDs) {
                try {
                    tag.putIntArray("nr", UUIDUtil.uuidToIntArray(UUID.fromString(uuid)));
                } catch (IllegalArgumentException ignored) {
                    tag.putString("nr", uuid);
                }
            }
            if (rotation != null && !rotation.equals(ZERO)) {
                tag.put("rot", vecToList(rotation));
            }
            if (origin != null && !origin.equals(ZERO)) {
                tag.put("piv", vecToList(origin));
            }

            // realistically this is only applicable if it's false.
            if (Boolean.FALSE.equals(visibility))
                tag.putBoolean("vsb", false);

            attachCollections(context, uuid, tag);

            return tag;
        }
    }

    public static class UnknownElement extends Element {
        /* idk */

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            // Never export.
            return null;
        }
    }

    public static class CubeElement extends Element {
        // type = "cube"
        FiguraVec3 from;
        FiguraVec3 to;

        float inflate;

        Map<String, CubeFace> faces;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = super.toNBT(context);
            if (tag == null) return null;

            if (from != null && !from.equals(ZERO)) tag.put("f", vecToList(from));
            if (to != null && !to.equals(ZERO)) tag.put("t", vecToList(to));
            if (inflate != 0f)
                tag.putFloat("inf", inflate);

            CompoundTag cubeData = new CompoundTag();
            for (String face : CubeFace.FACES) {
                CubeFace faceData;
                if ((faceData = faces.get(face)) != null) {
                    CompoundTag faceNBT = faceData.toNBT(context);
                    if (faceNBT != null)
                        cubeData.put(String.valueOf(face.charAt(0)), faceNBT);
                }
            }

            tag.put("cube_data", cubeData);

            return tag;
        }
    }

    public static class CubeFace implements NBTRepresentation<CompoundTag> {
        static final String[] FACES = new String[]{"north", "south", "west", "east", "up", "down"};

        FiguraVec4 uv;
        float rotation;
        @Nullable Integer texture;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            if (texture == null)
                return null;
            CompoundTag tag = new CompoundTag();
            Integer textureID = context.getTextureGlobalID(texture);
            if (textureID == null) return null;
            tag.putInt("tex", textureID);
            if (rotation != 0f)
                tag.putFloat("rot", rotation);
            if (uv != null && !uv.equals(ZERO4)) {
                FiguraVec2 size = context.getTextureFixedSize(texture);
                FiguraVec4 sizeTwice = FiguraVec4.of(size.x, size.y, size.x, size.y);
                // this is the order it is because otherwise we'd be mutating uv
                FiguraVec4 corrected = sizeTwice.multiply(uv);
                tag.put("uv", vecToList(corrected));
            }
            return tag;
        }
    }

    public static class MeshElement extends Element {
        /// map of face ID(?) to face
        Map<String, MeshFace> faces;
        /// map of vertex ID to offsets
        Map<String, FiguraVec3> vertices;

        // default "flat", alternative "smooth"
        // smooth: set 'smo' to true (note: since meshes aren't groups, only setting it if true is fine)
        String shading;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = super.toNBT(context);
            if (tag == null) return null;

            if (Objects.equals(shading, "smooth"))
                tag.putBoolean("smo", true);

            CompoundTag meshData = new CompoundTag();

            HashMap<String, Integer> vert2idx = new HashMap<>();
            HashMap<String, FiguraVec3> vert2pos = new HashMap<>();
            ListTag vtx = new ListTag();

            int i = 0;
            for (Map.Entry<String, FiguraVec3> entry : vertices.entrySet()) {
                FiguraVec3 combined = entry.getValue().copy().add(origin);
                vert2idx.put(entry.getKey(), i++);
                vert2pos.put(entry.getKey(), combined);

                vtx.add(FloatTag.valueOf((float) combined.x));
                vtx.add(FloatTag.valueOf((float) combined.y));
                vtx.add(FloatTag.valueOf((float) combined.z));
            }

            // textures _and_ vertex counts, despite the name
            ListTag tex = new ListTag();
            ListTag uvs = new ListTag();
            ListTag fac = new ListTag();

            int intType = 0; // byte
            if (i > 0xff) intType = 1; // short
            if (i > 0xffff) intType = 2; // int

            for (Map.Entry<String, MeshFace> entry : faces.entrySet()) {
                MeshFace face = entry.getValue();
                if (face.texture == null || face.vertices == null || face.uv == null || face.vertices.length < 3 || face.vertices.length > 4)
                    continue;

                // so, want a face with... 16 sides?
                // ...or 4096 textures? (which will cause other problems)
                // format 0xTTTn
                Integer textureID = context.getTextureGlobalID(face.texture);
                if (textureID == null) continue;
                short texBase = (short) ((textureID << 4) + face.vertices.length);
                tex.add(ShortTag.valueOf(texBase));

                if (face.vertices.length == 4)
                    face.reorder(vert2pos);

                for (String vertID : face.vertices) {
                    Tag value = switch (intType) {
                        case 0 -> ByteTag.valueOf(vert2idx.get(vertID).byteValue());
                        case 1 -> ShortTag.valueOf(vert2idx.get(vertID).shortValue());
                        case 2 -> IntTag.valueOf(vert2idx.get(vertID));
                        default -> throw new IllegalStateException("bad int size key " + intType);
                    };
                    fac.add(value);

                    FiguraVec2 uv = face.uv.get(vertID);
                    FiguraVec2 fixedSize = context.getTextureFixedSize(face.texture);
                    uvs.add(FloatTag.valueOf((float) (uv.x * fixedSize.x)));
                    uvs.add(FloatTag.valueOf((float) (uv.y * fixedSize.y)));
                }
            }

            meshData.put("vtx", vtx);
            meshData.put("tex", tex);
            meshData.put("fac", fac);
            meshData.put("uvs", uvs);

            tag.put("mesh_data", meshData);
            return tag;
        }
    }

    public static class MeshFace {
        /// map of vertex ID to UV position
        Map<String, FiguraVec2> uv;
        /// list of vertex IDs
        String[] vertices;
        /// optional texture override
        Integer texture;

        public void reorder(Map<String, FiguraVec3> vertexIDtoPos) {
            if (vertices.length != 4) return;

            FiguraVec3 v1 = vertexIDtoPos.get(vertices[0]);
            FiguraVec3 v2 = vertexIDtoPos.get(vertices[1]);
            FiguraVec3 v3 = vertexIDtoPos.get(vertices[2]);
            FiguraVec3 v4 = vertexIDtoPos.get(vertices[3]);

            if (testOppositeSides(v2, v3, v1, v4)) {
                vertices = new String[]{
                        vertices[2],
                        vertices[0],
                        vertices[1],
                        vertices[3]
                };
            } else if (testOppositeSides(v1, v2, v3, v4)) {
                vertices = new String[]{
                        vertices[0],
                        vertices[2],
                        vertices[1],
                        vertices[3]
                };
            }
        }

        private static final FiguraVec3
                temp1 = new FiguraVec3(),
                temp2 = new FiguraVec3(),
                temp3 = new FiguraVec3(),
                temp4 = new FiguraVec3();

        /**
         * Checks if the two points are on opposite sides of the line formed by the two other points
         */
        private static boolean testOppositeSides(FiguraVec3 line1, FiguraVec3 line2, FiguraVec3 point1, FiguraVec3 point2) {
            // why does this work? I don't know
            temp1.set(line1);
            temp2.set(line2);
            temp3.set(point1);
            temp4.set(point2);

            temp2.subtract(temp1);
            temp3.subtract(temp1);
            temp4.subtract(temp1);

            temp1.set(temp2);
            temp1.cross(temp3);
            temp2.cross(temp4);

            return temp1.dot(temp2) < 0;
        }
    }

    public static class PointElement extends Element {
        FiguraVec3 position;

        @Override
        public @Nullable CompoundTag toNBT(BlockbenchParser2.Intermediary context) {
            CompoundTag tag = super.toNBT(context);
            if (tag == null) return null;
            if (position != null && origin == null && !position.equals(ZERO))
                tag.put("piv", vecToList(position));

            // Allow parent types for these; this is mostly useful for the pivot-type parent types
            parseParent(name, tag);

            return tag;
        }
    }

    public static class Animation {
        String name;

        // once, loop, hold
        @Nullable String loop;
        @Nullable Boolean override;
        float length;

        @Nullable String anim_time_update;
        @Nullable String blend_weight;
        @Nullable String start_delay;
        @Nullable String loop_delay;

        // key is a UUID, or the string "effects"
        @Nullable Map<String, Animator> animators;
    }

    public static class Animator {
        // why does it do this? No clue
        /**
         * <a href="https://github.com/JannisX11/blockbench/blob/603201853499087aa3aa6a14406a061f27d4898f/js/io/formats/bbmodel.js#L72-L98">
         * v5 flipped the X and Y axes on rotations.
         * </a>
         */
        public static final FiguraVec3 v5_ROT_TRANS = FiguraVec3.of(-1, -1, 1);
        /**
         * <a href="https://github.com/JannisX11/blockbench/blob/603201853499087aa3aa6a14406a061f27d4898f/js/io/formats/bbmodel.js#L72-L98">
         * v5 flipped the X axis on positions.
         * </a>
         */
        public static final FiguraVec3 v5_POS_TRANS = FiguraVec3.of(-1, 1, 1);

        private static final FiguraVec3 DO_NOTHING = FiguraVec3.of(1, 1, 1);

        // so it turns out (double)0.1f != 0.1; it's actually 0.10000000149011612
        // so that's what we need to check against; we're taking a float and shoving it into a double and back
        private static final FiguraVec3 LEFT_TIMING = FiguraVec3.of(-0.1f, -0.1f, -0.1f);
        private static final FiguraVec3 RIGHT_TIMING = FiguraVec3.of(0.1f, 0.1f, 0.1f);

        String name;
        String type;
        @Nullable Boolean rotation_global;
        // This goes unused in Figura, but I'm leaving it here in the hopes
        // that someone else picks it up, maybe
        @Nullable Boolean quaternion_interpolation;
        @Nullable List<Keyframe> keyframes;

        // okay so that qualified class name is horrible, but it's what we have

        /**
         * Get a NBT tag for this animator.
         *
         * @param animContext what {@link BlockbenchParser2.Intermediary.AnimationRepresentation} this is
         * @param isV5        if this is a v5 model; flips X and Y components of some things
         * @return the NBT
         */
        public @Nullable CompoundTag getNBT(BlockbenchParser2.Intermediary.AnimationRepresentation animContext,
                                            boolean isV5) {
            if ("effect".equals(type)) throw new RuntimeException("Shouldn't be attaching FX animators to parts!");

            FiguraVec3 rotTrans = isV5 ? v5_ROT_TRANS : DO_NOTHING;
            FiguraVec3 posTrans = isV5 ? v5_POS_TRANS : DO_NOTHING;

            ListTag rot = new ListTag();
            ListTag pos = new ListTag();
            ListTag scale = new ListTag();

            if (keyframes != null)
                for (Keyframe kf : keyframes) {
                    if (!(kf instanceof Keyframe.Keyframe3 kf3)) continue;

                    CompoundTag kfTag = new CompoundTag();
                    kfTag.putFloat("time", kf3.time);
                    kfTag.putString("int", kf3.interpolation != null ? kf3.interpolation : "linear");
                    Keyframe.Keyframe3.Data pre = kf3.data_points[0];

                    FiguraVec3 trans;
                    boolean transX = isV5 && (kf3.channel.equals("rotation") || kf3.channel.equals("position"));
                    boolean transY = isV5 && (kf3.channel.equals("rotation"));
                    switch (kf3.channel) {
                        case "rotation" -> trans = rotTrans;
                        case "position" -> trans = posTrans;
                        default -> trans = DO_NOTHING;
                    }

                    kfTag.put("pre", pre.toNBT(kf3.channel, transX, transY));
                    if (kf3.data_points.length > 1) {
                        Keyframe.Keyframe3.Data post = kf3.data_points[1];
                        kfTag.put("end", post.toNBT(kf3.channel, transX, transY));
                    }

                    // Bezier handles
                    if (kf3.bezier_left_value != null && !kf3.bezier_left_value.equals(ZERO)) {
                        kfTag.put("bl", vecToList(kf3.bezier_left_value.copy().multiply(trans)));
                    }
                    if (kf3.bezier_right_value != null && !kf3.bezier_right_value.equals(ZERO)) {
                        kfTag.put("br", vecToList(kf3.bezier_right_value.copy().multiply(trans)));
                    }
                    if (kf3.bezier_left_time != null && !kf3.bezier_left_time.equals(LEFT_TIMING)) {
                        kfTag.put("blt", vecToList(kf3.bezier_left_time));
                    }
                    if (kf3.bezier_right_time != null && !kf3.bezier_right_time.equals(RIGHT_TIMING)) {
                        kfTag.put("brt", vecToList(kf3.bezier_right_time));
                    }

                    switch (kf3.channel) {
                        case "position" -> pos.add(kfTag);
                        case "rotation" -> rot.add(kfTag);
                        case "scale" -> scale.add(kfTag);
                    }
                }

            CompoundTag animatorTag = new CompoundTag();
            CompoundTag channels = new CompoundTag();
            if (!rot.isEmpty()) {
                if (Boolean.TRUE.equals(rotation_global)) {
                    // ... does this work at all on the other end?
                    channels.put("grot", rot);
                } else {
                    channels.put("rot", rot);
                }
            }
            if (!pos.isEmpty()) channels.put("pos", pos);
            if (!scale.isEmpty()) channels.put("scl", scale);

            if (!channels.isEmpty()) {
                animatorTag.putInt("id", animContext.globalID);
                animatorTag.put("data", channels);
            } else {
                return null;
            }

            return animatorTag;
        }
    }

    public static class Keyframe {
        public static final TypeAdapterFactory ADAPTER_FACTORY =
                new GsonTypeByField<>(Keyframe.class, "channel")
                        .bind("position", Keyframe3.class)
                        .bind("rotation", Keyframe3.class)
                        .bind("scale", Keyframe3.class)
                        .bind("timeline", InstructionKeyframe.class)
                        .withFallback(Keyframe.class);

        String channel;
        @Nullable String interpolation;
        float time;

        // bezier interpolation
        FiguraVec3 bezier_left_time;
        FiguraVec3 bezier_left_value;
        FiguraVec3 bezier_right_time;
        FiguraVec3 bezier_right_value;

        public static class Keyframe3 extends Keyframe {
            public static class Data {
                String x;
                String y;
                String z;

                public ListTag toNBT(String channel, boolean invX, boolean invY) {
                    float fallback = channel.equals("scale") ? 1f : 0f;
                    Object x = kfData(this.x, fallback), y = kfData(this.y, fallback), z = kfData(this.z, fallback);

                    ListTag tag = new ListTag();
                    if (x instanceof Float xf && y instanceof Float yf && z instanceof Float zf) {
                        float xs = invX ? -1f : 1f;
                        float ys = invY ? -1f : 1f;
                        tag.add(FloatTag.valueOf(xf * xs));
                        tag.add(FloatTag.valueOf(yf * ys));
                        tag.add(FloatTag.valueOf(zf));
                    } else {
                        if (invX)
                            tag.add(StringTag.valueOf(BlockbenchV5Model.negateLua(String.valueOf(x))));
                        else
                            tag.add(StringTag.valueOf(String.valueOf(x)));
                        if (invY)
                            tag.add(StringTag.valueOf(BlockbenchV5Model.negateLua(String.valueOf(y))));
                        else
                            tag.add(StringTag.valueOf(String.valueOf(y)));
                        tag.add(StringTag.valueOf(String.valueOf(z)));
                    }

                    return tag;
                }
            }

            /// 1 or 2
            Data[] data_points;
        }

        public static class InstructionKeyframe extends Keyframe {
            public static class Data {
                String script;
            }

            /// should only be 1 item
            Data[] data_points;
        }

        public static Object kfData(String in, float fallback) {
            if (in == null || in.isBlank()) return fallback;
            try {
                return Float.parseFloat(in);
            } catch (NumberFormatException e) {
                return in;
            }
        }
    }

    public static class Texture {
        String name;
        @Nullable String source;
        @Nullable String relative_path;
        Float width;
        Float height;
        Float uv_width;
        Float uv_height;
    }

    public static class Collection implements UUIDReferable {
        String uuid;
        String name;

        /// uuid referables
        List<String> children;

        @Override
        public String getUUID() {
            return uuid;
        }
    }
}
