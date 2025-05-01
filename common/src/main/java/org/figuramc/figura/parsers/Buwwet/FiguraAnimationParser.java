// Class Version: 17
package org.figuramc.figura.parsers.Buwwet;

import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.figuramc.figura.utils.MathUtils;
import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPartReader;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Interpolation;
import com.google.gson.JsonArray;
import org.figuramc.figura.animation.TransformType;
import net.minecraft.nbt.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class FiguraAnimationParser
{
    private static <T> T checkIfNull(final T t, final T t2) {
        if (t == null) {
            return t2;
        }
        return t;
    }
    
    private static String checkIfEmpty(final String s, final String s2) {
        if (s == "") {
            return s2;
        }
        return s;
    }
    
    public static JsonElement animationToJson(final CompoundTag CompoundTag) {
        final JsonObject jsonObject = new JsonObject();
        for (final CompoundTag CompoundTag2 : CompoundTag.getList("anim", 10)) {
            if (CompoundTag2.contains("id")) {
                if (!CompoundTag2.contains("data")) {
                    continue;
                }
                final CompoundTag compound = CompoundTag2.getCompound("data");
                for (final String s2 : compound.getKeys()) {
                    final String s = s2;
                    TransformType transformType = switch (s2) {
                        case "pos" -> TransformType.POSITION;
                        case "rot" -> TransformType.ROTATION;
                        case "grot" -> TransformType.GLOBAL_ROT;
                        case "scl" -> TransformType.SCALE;
                        default -> null;
                    };
                    if (transformType == null) {
                        continue;
                    }
                    final JsonArray jsonArray = new JsonArray();
                    for (final CompoundTag CompoundTag3 : compound.getList(s, 10)) {
                        CompoundTag3.getFloat("time");
                        try {
                            Interpolation.valueOf(CompoundTag3.getString("int").toUpperCase());
                        }
                        catch (final Exception ex) {
                            FiguraMod.LOGGER.error("", ex);
                            continue;
                        }
                        Pair pair = FiguraModelPartReader.parseKeyframeData(CompoundTag3, "pre");
                        if (pair == null) {
                            pair = Pair.of((Object)FiguraVec3.of(), (Object)null);
                        }
                        if (FiguraModelPartReader.parseKeyframeData(CompoundTag3, "end") == null) {}
                        final FiguraVec3 of = FiguraVec3.of();
                        final FiguraVec3 of2 = FiguraVec3.of();
                        FiguraModelPartReader.readVec3(of, CompoundTag3, "bl");
                        FiguraModelPartReader.readVec3(of2, CompoundTag3, "br");
                        final FiguraVec3 of3 = FiguraVec3.of(-0.1, -0.1, -0.1);
                        final FiguraVec3 of4 = FiguraVec3.of(0.1, 0.1, 0.1);
                        FiguraModelPartReader.readVec3(of3, CompoundTag3, "blt");
                        FiguraModelPartReader.readVec3(of4, CompoundTag3, "brt");
                        of3.add(1.0, 1.0, 1.0);
                        MathUtils.clamp(of3, 0.0, 1.0);
                        MathUtils.clamp(of4, 0.0, 1.0);
                    }
                }
            }
        }
        return (JsonElement)jsonObject;
    }
    
    public static JsonArray createJsonAnimations(final HashMap<Integer, ArrayList<Pair<String, JsonElement>>> hashMap, final HashMap<Integer, CompoundTag> hashMap2) {
        final JsonArray jsonArray = new JsonArray();
        for (final Map.Entry entry : hashMap2.entrySet()) {
            final CompoundTag CompoundTag = (CompoundTag)entry.getValue();
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", CompoundTag.getString("name"));
            jsonObject.addProperty("loop", checkIfEmpty(CompoundTag.getString("loop"), "once"));
            jsonObject.addProperty("override", Boolean.valueOf(checkIfNull(CompoundTag.getBoolean("ovr"), false)));
            jsonObject.addProperty("length", (Number)CompoundTag.getFloat("len"));
            jsonObject.addProperty("anim_time_update", "");
            jsonObject.addProperty("blend_weight", "");
            jsonObject.addProperty("start_delay", "");
            jsonObject.addProperty("loop_delay", "");
            final JsonObject jsonObject2 = new JsonObject();
            for (final Pair pair : hashMap.get(entry.getKey())) {
                jsonObject2.add((String)pair.getFirst(), (JsonElement)pair.getSecond());
            }
            jsonObject.add("animators", (JsonElement)jsonObject2);
            jsonArray.add((JsonElement)jsonObject);
        }
        return jsonArray;
    }
    
    public static class AnimationKeyframeData
    {
        public String uuid;
        public String channel;
        public String interpolation;
        public boolean bezier_linked;
        public double[] bezier_left_time;
        public double[] bezier_left_value;
        public double[] bezier_right_time;
        public double[] bezier_right_value;
        public double[] datapoints;
        public float time;
        public float color;
        
        public AnimationKeyframeData(final String channel, final String interpolation, final boolean bezier_linked, final double[] bezier_left_time, final double[] bezier_left_value, final double[] bezier_right_time, final double[] bezier_right_value, final double[] datapoints, final float time) {
            this.color = -1.0f;
            this.uuid = UUID.randomUUID().toString();
            this.channel = channel;
            this.interpolation = interpolation;
            this.bezier_linked = bezier_linked;
            this.bezier_left_time = bezier_left_time;
            this.bezier_right_time = bezier_right_time;
            this.bezier_left_value = bezier_left_value;
            this.bezier_right_value = bezier_right_value;
            this.time = time;
            this.datapoints = datapoints;
        }
    }
    
    public static class AnimatorGroupData
    {
        public Integer animation_id;
        public AnimationKeyframeData[] keyframes;
        
        public AnimatorGroupData(final Integer animation_id, final AnimationKeyframeData[] keyframes) {
            this.animation_id = animation_id;
            this.keyframes = keyframes;
        }
        
        public static JsonObject animatorFromCode(final CompoundTag CompoundTag) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", "Effects");
            jsonObject.addProperty("type", "effect");
            final JsonArray jsonArray = new JsonArray();
            for (final CompoundTag CompoundTag2 : CompoundTag.getList("code", 10)) {
                final JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("uuid", UUID.randomUUID().toString());
                jsonObject2.addProperty("channel", "timeline");
                jsonObject2.addProperty("time", (Number)CompoundTag2.getFloat("time"));
                jsonObject2.addProperty("color", (Number)(-1));
                jsonObject2.addProperty("interpolation", "linear");
                final JsonObject jsonObject3 = new JsonObject();
                jsonObject3.addProperty("script", CompoundTag2.getString("src"));
                final JsonArray jsonArray2 = new JsonArray();
                jsonArray2.add((JsonElement)jsonObject3);
                jsonObject2.add("data_points", (JsonElement)jsonArray2);
                jsonObject2.addProperty("bezier_linked", Boolean.valueOf(true));
                jsonObject2.add("bezier_left_time", doubleArrayToJson(new double[] { -0.1, -0.1, -0.1 }));
                jsonObject2.add("bezier_right_time", doubleArrayToJson(new double[] { 0.1, 0.1, 0.1 }));
                jsonObject2.add("bezier_left_value", doubleArrayToJson(new double[] { 0.0, 0.0, 0.0 }));
                jsonObject2.add("bezier_right_value", doubleArrayToJson(new double[] { 0.0, 0.0, 0.0 }));
                jsonArray.add((JsonElement)jsonObject2);
            }
            jsonObject.add("keyframes", (JsonElement)jsonArray);
            return jsonObject;
        }
        
        public JsonArray toKeyframesJson() {
            final JsonArray jsonArray = new JsonArray();
            for (final AnimationKeyframeData animationKeyframeData : this.keyframes) {
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("uuid", animationKeyframeData.uuid);
                jsonObject.addProperty("channel", animationKeyframeData.channel);
                jsonObject.addProperty("time", (Number)animationKeyframeData.time);
                jsonObject.addProperty("color", (Number)animationKeyframeData.color);
                jsonObject.addProperty("interpolation", animationKeyframeData.interpolation);
                final JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("x", (Number)animationKeyframeData.datapoints[0]);
                jsonObject2.addProperty("y", (Number)animationKeyframeData.datapoints[1]);
                jsonObject2.addProperty("z", (Number)animationKeyframeData.datapoints[2]);
                final JsonArray jsonArray2 = new JsonArray();
                jsonArray2.add((JsonElement)jsonObject2);
                jsonObject.add("data_points", (JsonElement)jsonArray2);
                jsonObject.addProperty("bezier_linked", Boolean.valueOf(animationKeyframeData.bezier_linked));
                jsonObject.add("bezier_left_time", doubleArrayToJson(animationKeyframeData.bezier_left_time));
                jsonObject.add("bezier_right_time", doubleArrayToJson(animationKeyframeData.bezier_right_time));
                jsonObject.add("bezier_left_value", doubleArrayToJson(animationKeyframeData.bezier_left_value));
                jsonObject.add("bezier_right_value", doubleArrayToJson(animationKeyframeData.bezier_right_value));
                jsonArray.add((JsonElement)jsonObject);
            }
            return jsonArray;
        }
        
        private static JsonElement doubleArrayToJson(final double[] array) {
            final JsonArray jsonArray = new JsonArray();
            for (int length = array.length, i = 0; i < length; ++i) {
                jsonArray.add((Number)array[i]);
            }
            return (JsonElement)jsonArray;
        }
        
        static ArrayList<AnimatorGroupData> parseNbt(final CompoundTag CompoundTag) {
            final ArrayList list = new ArrayList();
            if (CompoundTag.contains("anim")) {
                for (final CompoundTag CompoundTag2 : CompoundTag.getList("anim", 10)) {
                    final Integer value = CompoundTag2.getInt("id");
                    final ArrayList<AnimationKeyframeData> list2 = new ArrayList<AnimationKeyframeData>();
                    final CompoundTag compound = CompoundTag2.getCompound("data");
                    for (final String s2 : compound.getKeys()) {
                        final String s = s2;
                        final String s4 = switch (s2) {
                            case "pos" -> "position";
                            case "rot" -> "rotation";
                            case "grot" -> "global_rotation";
                            case "scl" -> "scale";
                            default -> null;
                        };
                        for (final CompoundTag CompoundTag3 : compound.getList(s, 10)) {
                            final float float1 = CompoundTag3.getFloat("time");
                            final String string = CompoundTag3.getString("int");
                            Pair<FiguraVec3, String[]> pair = FiguraModelPartReader.parseKeyframeData(CompoundTag3, "pre");
                            if (pair == null) {
                                pair = FiguraModelPartReader.parseKeyframeData(CompoundTag3, "end");
                            }
                            else if (pair.getFirst() == null) {
                                pair = FiguraModelPartReader.parseKeyframeData(CompoundTag3, "end");
                                if (pair == null) {
                                    continue;
                                }
                                if (pair.getFirst() == null) {
                                    continue;
                                }
                            }
                            final FiguraVec3 of = FiguraVec3.of();
                            final FiguraVec3 of2 = FiguraVec3.of();
                            FiguraModelPartReader.readVec3(of, CompoundTag3, "bl");
                            FiguraModelPartReader.readVec3(of2, CompoundTag3, "br");
                            final FiguraVec3 of3 = FiguraVec3.of(-0.1, -0.1, -0.1);
                            final FiguraVec3 of4 = FiguraVec3.of(0.1, 0.1, 0.1);
                            FiguraModelPartReader.readVec3(of3, CompoundTag3, "blt");
                            FiguraModelPartReader.readVec3(of4, CompoundTag3, "brt");
                            of3.add(1.0, 1.0, 1.0);
                            list2.add(new AnimationKeyframeData(s4, string, true, MathUtils.clamp(of3, 0.0, 1.0).toArray(), of.toArray(), MathUtils.clamp(of4, 0.0, 1.0).toArray(), of2.toArray(), ((FiguraVec3)pair.getFirst()).toArray(), float1));
                        }
                    }
                    final AnimationKeyframeData[] array = new AnimationKeyframeData[list2.size()];
                    list2.toArray(array);
                    list.add(new AnimatorGroupData(value, array));
                }
            }
            return list;
        }
    }
}
