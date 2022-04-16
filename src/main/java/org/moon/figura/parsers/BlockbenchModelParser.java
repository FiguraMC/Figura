package org.moon.figura.parsers;

import com.google.gson.*;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//main class to convert a blockbench model (json) into nbt
//default fields are omitted from the nbt to save up space
//note: use the same instance for parsing multiple models for the same avatar
public class BlockbenchModelParser {

    //texture offset for diverse models
    private int textureOffset = 0;
    //animation offset
    private int animationOffset = 0;

    //used during the parser
    private final HashMap<String, NbtCompound> elementMap = new HashMap<>();
    private final HashMap<String, NbtList> animationMap = new HashMap<>();
    private final HashMap<String, TextureData> textureMap = new HashMap<>();
    private final HashMap<Integer, String> textureIdMap = new HashMap<>();

    //parser
    public ModelData parseModel(String json) {
        //parse json -> object
        Gson gson = new GsonBuilder().create();
        BlockbenchModel model = gson.fromJson(json, BlockbenchModel.class);

        //return lists
        List<NbtCompound> textureList = new ArrayList<>();
        List<NbtCompound> animationList = new ArrayList<>();

        //object -> nbt
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", model.name);

        //parse textures first
        //we want to save the textures in a separated list
        //we also want to fix the UV mismatch from the resolution and the texture
        //emissive textures are not put into the texture map, so we need to fix parts texture ids
        parseTextures(textureList, model.textures, model.resolution);

        //parse elements into a map of UUID (String) -> NbtCompound (the element)
        //later when parsing the outliner, we fetch the elements from this map
        parseElements(gson, model.elements);

        //parse animations
        //add the animation metadata to the animation list
        //but return a map with the group animation, as we will store it on the groups themselves
        parseAnimations(animationList, gson, model.animations);

        //add and parse the outliner
        nbt.put("chld", parseOutliner(gson, model.outliner));

        //clear variables used by the parser
        elementMap.clear();
        animationMap.clear();
        textureMap.clear();
        textureIdMap.clear();

        //return the parsed data
        return new ModelData(textureList, animationList, nbt);
    }

    // -- internal functions -- //

    private void parseTextures(List<NbtCompound> list, BlockbenchModel.Texture[] textures, BlockbenchModel.Resolution resolution) {
        if (textures == null)
            return;

        for (int i = 0; i < textures.length; i++) {
            String name = textures[i].name.replace(".png", "");
            String source = textures[i].source.replace("data:image/png;base64,", "");
            String renderType = textures[i].render_mode;

            //nbt
            NbtCompound compound = new NbtCompound();
            compound.putString("name", name);
            compound.putByteArray("src", source.getBytes());
            if (!renderType.equalsIgnoreCase("default"))
                compound.putString("type", renderType);
            list.add(compound);

            //lists
            textureIdMap.put(i, name);
            if (renderType.equalsIgnoreCase("default")) {
                int[] imageSize = getTextureSize(Base64.getDecoder().decode(source));
                textureMap.put(name, new TextureData(i + textureOffset, new float[]{(float) imageSize[0] / resolution.width, (float) imageSize[1] / resolution.height}));
            }
        }

        textureOffset += list.size();
    }

    private void parseElements(Gson gson, BlockbenchModel.Element[] elements) {
        for (BlockbenchModel.Element element : elements) {
            if (element.type == null)
                element.type = "cube";
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

            elementMap.put(id, nbt);
        }
    }

    private NbtCompound parseCubeFaces(Gson gson, JsonObject faces) {
        NbtCompound nbt = new NbtCompound();

        for (String cubeFace : BlockbenchModel.CubeFace.FACES) {
            if (!faces.has(cubeFace))
                continue;

            //convert face json to java object
            BlockbenchModel.CubeFace face = gson.fromJson(faces.getAsJsonObject(cubeFace), BlockbenchModel.CubeFace.class);

            //dont add null faces
            if (face.texture == null)
                continue;

            //parse texture
            TextureData texture = textureMap.get(textureIdMap.get(face.texture));
            if (texture == null)
                continue;

            NbtCompound faceNbt = new NbtCompound();
            faceNbt.putInt("tex", texture.id);

            //parse face
            if (face.rotation != 0f)
                faceNbt.putFloat("rot", face.rotation);

            //parse uv
            if (face.uv != null && notZero(face.uv)) {
                float[] uv = {face.uv[0] * texture.fixedSize[0], face.uv[1] * texture.fixedSize[1], face.uv[2] * texture.fixedSize[0], face.uv[3] * texture.fixedSize[1]};
                faceNbt.put("uv", toNbtList(uv));
            }

            nbt.put(String.valueOf(cubeFace.charAt(0)), faceNbt);
        }

        return nbt;
    }

    private NbtCompound parseMesh(Gson gson, JsonObject faces, JsonObject vertices) {
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

            //dont parse empty faces
            if (face.texture == null)
                continue;

            //parse texture
            TextureData texture = textureMap.get(textureIdMap.get(face.texture));
            if (texture == null)
                continue;

            NbtCompound faceNbt = new NbtCompound();
            faceNbt.putInt("tex", texture.id);

            //parse face vertices
            if (face.vertices != null) {
                NbtList faceVertices = new NbtList();
                for (String vertex : face.vertices)
                    faceVertices.add(NbtInt.of(verticesMap.get(vertex)));
                faceNbt.put("vtx", faceVertices);
            }

            //parse uv
            if (face.uv != null) {
                NbtCompound faceUV = new NbtCompound();
                for (Map.Entry<String, JsonElement> uvEntry : face.uv.entrySet()) {
                    float[] uv = toFloatArray(uvEntry.getValue());
                    float[] fixedUV = {uv[0] * texture.fixedSize[0], uv[1] * texture.fixedSize[1]};
                    faceUV.put(String.valueOf(verticesMap.get(uvEntry.getKey())), toNbtList(fixedUV));
                }
                faceNbt.put("uv", faceUV);
            }

            facesList.add(faceNbt);
        }

        nbt.put("vtx", verticesList);
        nbt.put("fac", facesList);
        return nbt;
    }

    private void parseAnimations(List<NbtCompound> list, Gson gson, BlockbenchModel.Animation[] animations) {
        if (animations == null)
            return;

        int i = 0;
        for (BlockbenchModel.Animation animation : animations) {
            NbtCompound animNbt = new NbtCompound();

            //animation metadata
            animNbt.putString("name", animation.name);
            animNbt.putString("loop", animation.loop);
            if (animation.override != null && animation.override)
                animNbt.putBoolean("ovr", true);
            if (animation.length != 0f)
                animNbt.putFloat("len", animation.length);

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

            //hacky solution to skip the for loop
            if (animation.animators == null)
                animation.animators = new JsonObject();

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
                    compound.putInt("id", i + animationOffset);
                    compound.put("kf", data);

                    if (animationMap.containsKey(id)) {
                        animationMap.get(id).add(compound);
                    } else {
                        NbtList nbt = new NbtList();
                        nbt.add(compound);
                        animationMap.put(id, nbt);
                    }
                }
            }

            list.add(animNbt);
            i++;
        }

        animationOffset += list.size();
    }

    private NbtList parseKeyFrameData(Gson gson, JsonObject object) {
        BlockbenchModel.KeyFrameData endFrameData = gson.fromJson(object, BlockbenchModel.KeyFrameData.class);

        NbtList nbt = new NbtList();
        nbt.add(NbtFloat.of(toFloat(endFrameData.x, 0f)));
        nbt.add(NbtFloat.of(toFloat(endFrameData.y, 0f)));
        nbt.add(NbtFloat.of(toFloat(endFrameData.z, 0f)));

        return nbt;
    }

    private NbtList parseOutliner(Gson gson, JsonArray outliner) {
        NbtList children = new NbtList();

        if (outliner == null)
            return children;

        for (JsonElement element : outliner) {
            //check if it is an ID first
            if (element instanceof JsonPrimitive) {
                if (elementMap.containsKey(element.getAsString()))
                    children.add(elementMap.get(element.getAsString()));

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
            if (group.rotation != null && notZero(group.rotation))
                groupNbt.put("rot", toNbtList(group.rotation));

            //parse children
            if (group.children != null && group.children.size() > 0)
                groupNbt.put("chld", parseOutliner(gson, group.children));

            //add animations
            if (animationMap.containsKey(group.uuid))
                groupNbt.put("anim", animationMap.get(group.uuid));

            children.add(groupNbt);
        }

        return children;
    }

    // -- helper functions -- //

    //converts a float array into a nbt list
    public static NbtList toNbtList(float[] floats) {
        NbtList list = new NbtList();

        int bestType = 0; //byte
        for (float f : floats) {
            if (Math.rint(f) - f == 0) {
                if (f < -127 || f >= 128)
                    bestType = 1; //short
                if (f < -16383 || f >= 16384) {
                    bestType = 2;
                    break;
                }
            } else {
                bestType = 2; //float
                break;
            }
        }

        for (float f : floats) {
            switch (bestType) {
                case 0 -> list.add(NbtByte.of((byte) f));
                case 1 -> list.add(NbtShort.of((short) f));
                case 2 -> list.add(NbtFloat.of(f));
            }
        }

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

    //get texture size
    public static int[] getTextureSize(byte[] texture) {
        int w = (int) texture[16] & 0xFF;
        w = (w << 8) + ((int) texture[17] & 0xFF);
        w = (w << 8) + ((int) texture[18] & 0xFF);
        w = (w << 8) + ((int) texture[19] & 0xFF);

        int h = (int) texture[20] & 0xFF;
        h = (h << 8) + ((int) texture[21] & 0xFF);
        h = (h << 8) + ((int) texture[22] & 0xFF);
        h = (h << 8) + ((int) texture[23] & 0xFF);

        return new int[]{w, h};
    }

    //dummy texture data
    private record TextureData(int id, float[] fixedSize) {}

    //dummy class containing the return object of the parser
    public record ModelData(List<NbtCompound> textureList, List<NbtCompound> animationList, NbtCompound modelNbt) {}
}
