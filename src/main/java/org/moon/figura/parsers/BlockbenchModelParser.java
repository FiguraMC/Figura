package org.moon.figura.parsers;

import com.google.gson.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;

//main class to convert a blockbench model (json) into nbt
//default fields are omitted from the nbt to save up space
public class BlockbenchModelParser {

    //parser
    public static NbtCompound parseModel(String json) {
        //parse json -> object
        Gson gson = new GsonBuilder().create();
        BlockbenchModel model = gson.fromJson(json, BlockbenchModel.class);

        //object -> nbt
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", model.name);

        //add texture size
        parseResolution(nbt, model.resolution);

        //add textures
        parseTextures(nbt, model.textures);

        //parse elements into a map of UUID (String) -> NbtCompound (the element)
        //later when parsing the outliner, we fetch the elements from this map
        HashMap<String, NbtCompound> elements = parseElements(gson, model.elements);

        //parse animations
        //add the animation metadata to the nbt
        //but return a map with the group animation, as we will store it on the groups themselves
        HashMap<String, NbtList> animations = parseAnimations(nbt, gson, model.animations);

        //add and parse the outliner
        nbt.put("model", parseOutliner(gson, model.outliner, elements, animations));

        //return the nbt
        return nbt;
    }

    // -- internal functions -- //

    private static void parseResolution(NbtCompound nbt, BlockbenchModel.Resolution res) {
        if (res == null)
            return;

        NbtCompound resolution = new NbtCompound();
        resolution.putInt("w", res.width);
        resolution.putInt("h", res.height);

        nbt.put("res", resolution);
    }

    private static void parseTextures(NbtCompound nbt, BlockbenchModel.Texture[] textures) {
        if (textures == null)
            return;

        NbtList list = new NbtList();

        for (BlockbenchModel.Texture texture : textures) {
            NbtCompound compound = new NbtCompound();
            compound.putString("name", texture.name.replace(".png", ""));
            compound.putString("src", texture.source.replace("data:image/png;base64,", ""));
            list.add(compound);
        }

        nbt.put("tex", list);
    }

    private static HashMap<String, NbtCompound> parseElements(Gson gson, BlockbenchModel.Element[] elements) {
        HashMap<String, NbtCompound> map = new HashMap<>();

        if (elements == null)
            return map;

        for (BlockbenchModel.Element element : elements) {
            if (!element.type.equalsIgnoreCase("cube") && !element.type.equalsIgnoreCase("mesh"))
                continue;

            //temp variables
            String id = element.uuid;
            NbtCompound nbt = new NbtCompound();

            //parse fields
            nbt.putString("name", element.name);
            nbt.putString("type", element.type);

            //parse transform data
            if (element.from != null && notZero(element.from))
                nbt.put("f", toNbtList(element.from));
            if (element.to != null && notZero(element.to))
                nbt.put("t", toNbtList(element.to));
            if (element.rotation != null && notZero(element.rotation))
                nbt.put("rot", toNbtList(element.rotation));
            if (element.origin != null && notZero(element.origin))
                nbt.put("piv", toNbtList(element.origin));
            if (element.inflate != 0f)
                nbt.putFloat("inf", element.inflate);

            if (element.visibility != null && !element.visibility)
                nbt.putBoolean("vsb", false);

            //parse faces
            NbtCompound data;
            if (element.type.equalsIgnoreCase("cube")) {
                data = parseCubeFaces(gson, element.faces);
            } else {
                data = parseMesh(gson, element.faces, element.vertices);
            }
            nbt.put("data", data);

            map.put(id, nbt);
        }

        return map;
    }

    private static NbtCompound parseCubeFaces(Gson gson, JsonObject faces) {
        NbtCompound nbt = new NbtCompound();

        for (String cubeFace : BlockbenchModel.CubeFace.FACES) {
            if (!faces.has(cubeFace))
                continue;

            //convert face json to java object
            BlockbenchModel.CubeFace face = gson.fromJson(faces.getAsJsonObject(cubeFace), BlockbenchModel.CubeFace.class);

            //parse face
            NbtCompound faceNbt = new NbtCompound();

            if (face.uv != null && notZero(face.uv))
                faceNbt.put("uv", toNbtList(face.uv));
            if (face.rotation != 0f)
                faceNbt.putFloat("rot", face.rotation);
            if (face.texture != null)
                faceNbt.putInt("tex", face.texture);

            nbt.put(String.valueOf(cubeFace.charAt(0)), faceNbt);
        }

        return nbt;
    }

    private static NbtCompound parseMesh(Gson gson, JsonObject faces, JsonObject vertices) {
        NbtCompound nbt = new NbtCompound();

        //parse vertices first, as the faces will reference it later
        //we are going to save them in a String -> Integer map
        //the map will be preserved since it is very common to meshes share the same vertices,
        //so we can reduce even more file size
        HashMap<String, Integer> verticesMap = new HashMap<>();
        NbtList verticesList = new NbtList();

        int index = 0;
        for (Map.Entry<String, JsonElement> entry : vertices.entrySet()) {
            verticesMap.put(entry.getKey(), index);
            verticesList.add(index, toNbtList(toFloatArray(entry.getValue())));
            index++;
        }

        //parse faces
        NbtList facesList = new NbtList();
        for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
            //convert json to java object
            BlockbenchModel.MeshFace face = gson.fromJson(entry.getValue(), BlockbenchModel.MeshFace.class);

            //parse face
            NbtCompound faceNbt = new NbtCompound();

            if (face.texture != null)
                faceNbt.putInt("tex", face.texture);

            //parse face vertices
            if (face.vertices != null) {
                NbtList faceVertices = new NbtList();
                for (String vertex : face.vertices)
                    faceVertices.add(NbtInt.of(verticesMap.get(vertex)));
                faceNbt.put("vtx", faceVertices);
            }

            //parse face uv
            if (face.uv != null) {
                NbtCompound faceUV = new NbtCompound();
                for (Map.Entry<String, JsonElement> uvEntry : face.uv.entrySet())
                    faceUV.put(String.valueOf(verticesMap.get(uvEntry.getKey())), toNbtList(toFloatArray(uvEntry.getValue())));
                faceNbt.put("uv", faceUV);
            }

            facesList.add(faceNbt);
        }

        nbt.put("vtx", verticesList);
        nbt.put("fac", facesList);
        return nbt;
    }

    private static HashMap<String, NbtList> parseAnimations(NbtCompound nbt, Gson gson, BlockbenchModel.Animation[] animations) {
        HashMap<String, NbtList> animationMap = new HashMap<>();

        if (animations == null)
            return animationMap;

        int i = 0;
        NbtList animationList = new NbtList();
        for (BlockbenchModel.Animation animation : animations) {
            NbtCompound animNbt = new NbtCompound();

            //animation metadata
            animNbt.putString("name", animation.name);
            animNbt.putString("loop", animation.loop);
            if (animation.override != null && animation.override)
                animNbt.putBoolean("ovr", true);
            if (animation.length != 0f)
                animNbt.putFloat("len", animation.length);
            if (animation.snapping != 24f)
                animNbt.putFloat("snp", animation.snapping);

            float offset = toFloat(animation.anim_time_update, 0f);
            if (offset != 0f)
                animNbt.putFloat("off", offset);

            float blend = toFloat(animation.blend_weight, 1f);
            if (blend != 1f)
                animNbt.putFloat("bld", blend);

            float startDelay = toFloat(animation.start_delay, 0f);
            if (startDelay != 0f)
                animNbt.putFloat("sdel", startDelay);

            float loopDelay = toFloat(animation.loop_delay, 0f);
            if (loopDelay != 0f)
                animNbt.putFloat("ldel", loopDelay);

            //animation group data
            for (Map.Entry<String, JsonElement> entry : animation.animators.entrySet()) {
                String id = entry.getKey();
                boolean effect = id.equalsIgnoreCase("effects");
                NbtList data = new NbtList();

                //parse keyframes
                for (JsonElement keyframeJson : entry.getValue().getAsJsonObject().get("keyframes").getAsJsonArray()) {
                    BlockbenchModel.KeyFrame keyFrame = gson.fromJson(keyframeJson, BlockbenchModel.KeyFrame.class);

                    if (effect && !keyFrame.channel.equalsIgnoreCase("timeline"))
                        continue;

                    NbtCompound keyframeNbt = new NbtCompound();
                    keyframeNbt.putFloat("time", keyFrame.time);

                    if (effect) {
                        keyframeNbt.putString("src", keyFrame.data_points.get(0).getAsJsonObject().get("script").getAsString());
                    } else {
                        keyframeNbt.putString("ch", keyFrame.channel);
                        keyframeNbt.putString("int", keyFrame.interpolation);

                        //pre
                        JsonObject dataPoints = keyFrame.data_points.get(0).getAsJsonObject();
                        keyframeNbt.put("pre", parseKeyFrameData(gson, dataPoints));

                        //end
                        if (keyFrame.data_points.size() > 1) {
                            JsonObject endDataPoints = keyFrame.data_points.get(1).getAsJsonObject();
                            keyframeNbt.put("end", parseKeyFrameData(gson, endDataPoints));
                        }
                    }

                    data.add(keyframeNbt);
                }

                //add to nbt
                if (effect) {
                    animNbt.put("code", data);
                } else {
                    NbtCompound compound = new NbtCompound();
                    compound.putInt("id", i);
                    compound.put("kf", data);

                    if (animationMap.containsKey(id)) {
                        animationMap.get(id).add(compound);
                    } else {
                        NbtList list = new NbtList();
                        list.add(compound);
                        animationMap.put(id, list);
                    }
                }
            }

            animationList.add(animNbt);
            i++;
        }

        nbt.put("anim", animationList);
        return animationMap;
    }

    private static NbtList parseKeyFrameData(Gson gson, JsonObject object) {
        BlockbenchModel.KeyFrameData endFrameData = gson.fromJson(object, BlockbenchModel.KeyFrameData.class);

        NbtList nbt = new NbtList();
        nbt.add(NbtFloat.of(toFloat(endFrameData.x, 0f)));
        nbt.add(NbtFloat.of(toFloat(endFrameData.y, 0f)));
        nbt.add(NbtFloat.of(toFloat(endFrameData.z, 0f)));

        return nbt;
    }

    private static NbtList parseOutliner(Gson gson, JsonArray outliner, HashMap<String, NbtCompound> elements, HashMap<String, NbtList> animations) {
        NbtList children = new NbtList();

        if (outliner == null)
            return children;

        for (JsonElement element : outliner) {
            //check if it is an ID first
            if (element instanceof JsonPrimitive) {
                if (elements.containsKey(element.getAsString()))
                    children.add(elements.get(element.getAsString()));

                continue;
            }

            //then parse as GroupElement (outliner)
            NbtCompound groupNbt = new NbtCompound();
            BlockbenchModel.GroupElement group = gson.fromJson(element, BlockbenchModel.GroupElement.class);

            //parse fields
            groupNbt.putString("name", group.name);
            if (group.visibility != null && !group.visibility)
                groupNbt.putBoolean("vsb", false);

            //parse transforms
            if (group.origin != null && notZero(group.origin))
                groupNbt.put("piv", toNbtList(group.origin));

            //parse children
            if (group.children != null && group.children.size() > 0)
                groupNbt.put("chld", parseOutliner(gson, group.children, elements, animations));

            //add animations
            if (animations.containsKey(group.uuid))
                groupNbt.put("anim", animations.get(group.uuid));

            children.add(groupNbt);
        }

        return children;
    }

    // -- helper functions -- //

    //converts a float array into a nbt list
    public static NbtList toNbtList(float[] floats) {
        NbtList list = new NbtList();

        for (float f : floats)
            list.add(NbtFloat.of(f));

        return list;
    }

    //extract a float array from a JsonElement (unsafe)
    public static float[] toFloatArray(JsonElement element) {
        JsonArray array = element.getAsJsonArray();
        float[] f = new float[array.size()];

        int i = 0;
        for (JsonElement jsonElement : array) {
            f[i] = jsonElement.getAsFloat();
            i++;
        }

        return f;
    }

    //check if a float array is not composed of only zeros
    public static boolean notZero(float[] floats) {
        boolean zero = true;

        for (float f : floats) {
            if (f != 0) {
                zero = false;
                break;
            }
        }

        return !zero;
    }

    //try converting a String to float, with a fallback
    public static float toFloat(String input, float fallback) {
        try {
            return Float.parseFloat(input);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
