package org.figuramc.figura.parsers.Buwwet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.animation.Animation;
import org.figuramc.figura.animation.Interpolation;
import org.figuramc.figura.animation.Keyframe;
import org.figuramc.figura.animation.TransformType;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPartReader;
import org.figuramc.figura.utils.MathUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.figuramc.figura.model.FiguraModelPartReader.parseKeyframeData;
import static org.figuramc.figura.model.FiguraModelPartReader.readVec3;

public class FiguraAnimationParser {
    public static class AnimatorGroupData {
        public Integer animation_id;
        // Stores the keyframes that are applied to this particular group
        public AnimationKeyframeData[] keyframes;

        public AnimatorGroupData(Integer animation_id, AnimationKeyframeData[] keyframes) {
            this.animation_id = animation_id;
            this.keyframes = keyframes;
        }
        // Returns the keyframes as BlockBench json.
        public JsonArray toKeyframesJson() {
            JsonArray keyframesJson = new JsonArray();
            // Turn each keyframe into json (quite easy as the fields are named the same)
            for (AnimationKeyframeData keyframeData : this.keyframes) {
                // I think I could've just used the Gson converter thing but whatever xd
                JsonObject keyframeJson = new JsonObject();

                keyframeJson.addProperty("uuid", keyframeData.uuid);

                keyframeJson.addProperty("channel", keyframeData.channel);
                keyframeJson.addProperty("time", keyframeData.time);
                keyframeJson.addProperty("color", keyframeData.color);
                keyframeJson.addProperty("interpolation", keyframeData.interpolation);
                // add the data points
                JsonObject keyframeDataPoints = new JsonObject();
                keyframeDataPoints.addProperty("x", keyframeData.datapoints[0]);
                keyframeDataPoints.addProperty("y", keyframeData.datapoints[1]);
                keyframeDataPoints.addProperty("z", keyframeData.datapoints[2]);
                // BlockBench expects an array with only this.
                JsonArray keyframeDataPointHolder = new JsonArray();
                keyframeDataPointHolder.add(keyframeDataPoints);
                keyframeJson.add("data_points", keyframeDataPointHolder);

                keyframeJson.addProperty("bezier_linked", keyframeData.bezier_linked);
                keyframeJson.add("bezier_left_time", doubleArrayToJson(keyframeData.bezier_left_time));
                keyframeJson.add("bezier_right_time", doubleArrayToJson(keyframeData.bezier_right_time));
                keyframeJson.add("bezier_left_value", doubleArrayToJson(keyframeData.bezier_left_value));
                keyframeJson.add("bezier_right_value", doubleArrayToJson(keyframeData.bezier_right_value));

                // Done building
                keyframesJson.add(keyframeJson);
            }

            return keyframesJson;
        }

        private JsonElement doubleArrayToJson(double[] vec) {
            JsonArray array = new JsonArray();

            for (double d : vec) {
                array.add(d);
            }

            return array;
        }

        static ArrayList<AnimatorGroupData> parseNbt(CompoundTag nbt) {
            ArrayList<AnimatorGroupData> animations = new ArrayList<>();

            if (nbt.contains("anim")) {
                // Group has animation data.
                //FiguraMod.LOGGER.info("Group has anim data.");
                for (Tag nbtAnimationRaw : nbt.getList("anim", Tag.TAG_COMPOUND)) {
                    CompoundTag nbtAnimation = (CompoundTag) nbtAnimationRaw;

                    Integer animId = nbtAnimation.getInt("id");
                    ArrayList<AnimationKeyframeData> keyframes = new ArrayList<>();

                    // Loop through all the channels of the animation.
                    CompoundTag animNbtData = nbtAnimation.getCompound("data");
                    for (String nbtAnimChannel : animNbtData.getAllKeys()) {
                        String animChannel = switch (nbtAnimChannel) {
                            case "pos" -> "position";
                            case "rot" -> "rotation";
                            //  TODO: I don't know what this is.
                            case "grot" -> "global_rotation";
                            case "scl" -> "scale";
                            default -> null;
                        };

                        // Keyframe data
                        for (Tag keyframeTag : animNbtData.getList(nbtAnimChannel, Tag.TAG_COMPOUND)) {
                            CompoundTag keyframeNbt = (CompoundTag) keyframeTag;
                            float time = keyframeNbt.getFloat("time");
                            String interpolation = keyframeNbt.getString("int");

                            Pair<FiguraVec3, String[]> pre = parseKeyframeData(keyframeNbt, "pre");
                            if (pre == null) pre = Pair.of(FiguraVec3.of(), null);
                            // TODO: haven't seen end, but pre seems to be the value thing
                            Pair<FiguraVec3, String[]> end = parseKeyframeData(keyframeNbt, "end");
                            //if (end == null) end = pre;



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


                            AnimationKeyframeData keyframeData = new AnimationKeyframeData(
                                    animChannel, interpolation, true, bezierLeftTime.toArray(), bezierLeft.toArray(),
                                    bezierRightTime.toArray(), bezierRight.toArray(), pre.getFirst().toArray(), time
                            );
                            keyframes.add(keyframeData);
                            //keyframes.add(new Keyframe(owner, animation, time, interpolation, pre, end, bezierLeft, bezierRight, bezierLeftTime, bezierRightTime));
                        }
                    }

                    // Return the animation (at least just this one fragment of it)
                    AnimationKeyframeData[] keyframeArray = new AnimationKeyframeData[keyframes.size()];
                    keyframes.toArray(keyframeArray);
                    animations.add(new AnimatorGroupData(animId, keyframeArray));

                }
            }

            return animations;
        }
    }

    public static class AnimationKeyframeData {
        public String uuid;
        public String channel;
        public String interpolation;
        public boolean bezier_linked;
        public double[] bezier_left_time;
        public double[] bezier_left_value;
        public double[] bezier_right_time;
        public double[] bezier_right_value;

        // xyz
        public double[] datapoints;
        public float time;
        // no clue area
        public float color = -1;

        public AnimationKeyframeData(String channel, String interpolation, boolean bezier_linked, double[] bezier_left_time, double[] bezier_left_value, double[] bezier_right_time, double[] bezier_right_value, double[] datapoints, float time) {
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

    private static <T> T checkIfNull(T var, T fallback) {
        if (var == null) {
            return fallback;
        } else {
            return var;
        }
    }

    private static String checkIfEmpty(String var, String fallback) {
        if (var == "") {
            return fallback;
        } else {
            return var;
        }
    }

    public static JsonElement animationToJson(CompoundTag rootNbt) {
        JsonObject animationsJson = new JsonObject();

        ListTag nbt = rootNbt.getList("anim", Tag.TAG_COMPOUND);
        // Loop for each animation
        for (Tag tag : nbt) {
            CompoundTag compound = (CompoundTag) tag;
            //Animation animation;

            if (!compound.contains("id") || !compound.contains("data"))
                continue;

            // Define the animation




            CompoundTag animNbt = compound.getCompound("data");


            // Loop through the animation's channels
            for (String channelString : animNbt.getAllKeys()) {
                TransformType type = switch (channelString) {
                    case "pos" -> TransformType.POSITION;
                    case "rot" -> TransformType.ROTATION;
                    case "grot" -> TransformType.GLOBAL_ROT;
                    case "scl" -> TransformType.SCALE;
                    default -> null;
                };

                if (type == null)
                    continue;

                JsonArray keyframes = new JsonArray();
                ListTag keyframeList = animNbt.getList(channelString, Tag.TAG_COMPOUND);

                // Keyframe data
                for (Tag keyframeTag : keyframeList) {
                    CompoundTag keyframeNbt = (CompoundTag) keyframeTag;
                    float time = keyframeNbt.getFloat("time");
                    Interpolation interpolation;
                    try {
                        interpolation = Interpolation.valueOf(keyframeNbt.getString("int").toUpperCase());
                    } catch (Exception e) {
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

                    //keyframes.add(new Keyframe(owner, animation, time, interpolation, pre, end, bezierLeft, bezierRight, bezierLeftTime, bezierRightTime));
                }
            }
        }

        return animationsJson;

    }

    public static JsonArray createJsonAnimations(HashMap<Integer, ArrayList<Pair<String, JsonElement>>> animatorArray, ArrayList<CompoundTag> modelAnimRaw) {
        JsonArray animations = new JsonArray();

        int animationId = 0;
        for (CompoundTag modelAnimNbt : modelAnimRaw) {
            // Create the animation
            JsonObject animationJson = new JsonObject();
            animationJson.addProperty("name", modelAnimNbt.getString("name"));
            animationJson.addProperty("loop", checkIfEmpty(modelAnimNbt.getString("loop"), "once"));
            animationJson.addProperty("override", checkIfNull(modelAnimNbt.getBoolean("ovr"), false));

            animationJson.addProperty("length", modelAnimNbt.getFloat("len"));
            animationJson.addProperty("anim_time_update", "");
            animationJson.addProperty("blend_weight", "");
            animationJson.addProperty("start_delay", "");
            animationJson.addProperty("loop_delay", "");

            // append all the animators
            JsonObject animatorsJson = new JsonObject();
            for (Pair<String, JsonElement> animator : animatorArray.get(animationId)) {
                animatorsJson.add(animator.getFirst(), animator.getSecond());
            }
            animationJson.add("animators", animatorsJson);


            animations.add(animationJson);
            animationId++;
        }

        return animations;
    }
}
