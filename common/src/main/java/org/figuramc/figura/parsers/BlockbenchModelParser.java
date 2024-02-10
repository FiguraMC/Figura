package org.figuramc.figura.parsers;

import com.google.gson.*;
import net.minecraft.nbt.*;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.FiguraMod;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

//main class to convert a blockbench model (json) into nbt
//default fields are omitted from the nbt to save up space
//note: use the same instance for parsing multiple models for the same avatar
public class BlockbenchModelParser {

    private final static Gson GSON = new GsonBuilder().create();

    //offsets for usage of diverse models
    private int textureOffset = 0;
    private int animationOffset = 0;

    //used during the parser
    private final HashMap<String, CompoundTag> elementMap = new HashMap<>();
    private final HashMap<String, ListTag> animationMap = new HashMap<>();
    private final HashMap<String, TextureData> textureMap = new HashMap<>();
    private final HashMap<Integer, String> textureIdMap = new HashMap<>();

    //parser
    public ModelData parseModel(Path avatarFolder, Path sourceFile, String json, String modelName, String folders) throws Exception {
        // parse json -> object
        BlockbenchModel model = GSON.fromJson(json, BlockbenchModel.class);

        //meta check
        if (!model.meta.model_format.equals("free") && !model.meta.model_format.contains(FiguraMod.MOD_ID))
            throw new Exception("Model \"" + modelName + "\" have an incompatible model format. Compatibility is limited to \"Generic Model\" format and third-party " + FiguraMod.MOD_NAME + " specific formats");
        if (Integer.parseInt(model.meta.format_version.split("\\.")[0]) < 4)
            throw new Exception("Model \"" + modelName + "\" was created using a version too old (" + model.meta.format_version + ") of Blockbench. Minimum compatible version is 4.0");

        //return lists
        CompoundTag textures = new CompoundTag();
        List<CompoundTag> animationList = new ArrayList<>();

        //object -> nbt
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", modelName);
        parseParent(modelName, nbt);

        //parse textures first
        //we want to save the textures in a separated list
        //we also want to fix the UV mismatch from the resolution and the texture
        //emissive textures are not put into the texture map, so we need to fix parts texture ids
        parseTextures(avatarFolder, sourceFile, folders, modelName, textures, model.textures, model.resolution);

        //parse elements into a map of UUID (String) -> NbtCompound (the element)
        //later when parsing the outliner, we fetch the elements from this map
        parseElements(model.elements);

        //parse animations
        //add the animation metadata to the animation list
        //but return a map with the group animation, as we will store it on the groups themselves
        parseAnimations(animationList, model.animations, modelName, folders);

        //add and parse the outliner
        nbt.put("chld", parseOutliner(model.outliner, true));

        //clear variables used by the parser
        elementMap.clear();
        animationMap.clear();
        textureMap.clear();
        textureIdMap.clear();

        //return the parsed data
        return new ModelData(textures, animationList, nbt);
    }

    public static void parseParent(String name, CompoundTag nbt) {
        ParentType parentType = ParentType.get(name);
        if (parentType != ParentType.None)
            nbt.putString("pt", parentType.name());
    }

    // -- internal functions -- //

    private void parseTextures(Path avatar, Path sourceFile, String folders, String modelName, CompoundTag texturesNbt, BlockbenchModel.Texture[] textures, BlockbenchModel.Resolution resolution) throws Exception {
        if (textures == null)
            return;

        //temp lists

        //used for retrieving texture data by name, so we can expand the same data
        LinkedHashMap<String, CompoundTag> texturesTemp = new LinkedHashMap<>();

        //used for storing the index of the specific texture name
        List<String> textureIndex = new ArrayList<>();

        //nbt stuff
        CompoundTag src = new CompoundTag();
        ListTag data = new ListTag();

        //read textures
        for (int i = 0; i < textures.length; i++) {
            BlockbenchModel.Texture texture = textures[i];

            //name
            String name = texture.name;
            if (name.endsWith(".png"))
                name = name.substring(0, name.length() - 4);

            //texture type
            String textureType;

            if (name.endsWith("_e")) {
                textureType = "e";
            } else if (name.endsWith("_n")) {
                textureType = "n";
            } else if (name.endsWith("_s")) {
                textureType = "s";
            } else {
                textureType = "d";
            }

            //parse the texture data
            String path;
            byte[] source;
            try {
                //check the file to load
                Path p = sourceFile.resolve(texture.relative_path);
                if (p.getFileSystem() == FileSystems.getDefault()) {
                    File f = p.toFile().getCanonicalFile();
                    p = f.toPath();
                    if (!f.exists()) throw new IllegalStateException("File do not exists!");
                } else {
                    p = p.normalize();
                    if (p.getFileSystem() != avatar.getFileSystem())
                        throw new IllegalStateException("File from outside the avatar folder!");
                }
                if (avatar.getNameCount() > 1) if (!p.startsWith(avatar)) throw new IllegalStateException("File from outside the avatar folder!");
                FiguraMod.debug("path is {}", p.toString());
                //load texture
                source = IOUtils.readFileBytes(p);
                path = avatar.relativize(p)
                        .toString()
                        .replace(p.getFileSystem().getSeparator(), ".");
                path = path.substring(0, path.length() - 4);

                //fix name
                name = folders + name;

                //feedback
                FiguraMod.debug("Loaded {} Texture \"{}\" from {}", textureType.toUpperCase(Locale.US), name, p);
            } catch (Exception e) {
                if (e instanceof IOException)
                    FiguraMod.LOGGER.error("", e);

                //otherwise, load from the source stored in the model
                source = Base64.getDecoder().decode(texture.source.substring("data:image/png;base64,".length()));
                path = folders + modelName + "." + name;
                FiguraMod.debug("Loaded {} Texture \"{}\" from {}", textureType.toUpperCase(Locale.US), name, path);
            }

            //add source nbt
            src.putByteArray(path, source);

            //fix texture name
            if (!textureType.equals("d"))
                name = name.substring(0, name.length() - 2);

            //add textures nbt
            if (texturesTemp.containsKey(name)) {
                CompoundTag textureContainer = texturesTemp.get(name);
                if (textureContainer.contains(textureType))
                    throw new Exception("Model \"" + modelName + "\" contains texture with duplicate name \"" + name + "\"");
                textureContainer.putString(textureType, path);
            } else {
                //create nbt
                CompoundTag compound = new CompoundTag();
                compound.putString(textureType, path);

                //add to temp lists
                texturesTemp.put(name, compound);
                textureIndex.add(name);
            }

            //used on the model conversion, so save the id as is
            textureIdMap.put(i, name);

            //generate the texture data
            if (!textureMap.containsKey(name)) {
                //id is generated by the position of the name in the list
                int id = textureIndex.indexOf(name) + textureOffset;

                //fix texture size for more speed
                float[] fixedSize;
                if (texture.width != null) {
                    fixedSize = new float[]{(float) texture.width / texture.uv_width, (float) texture.height / texture.uv_height};
                }
                else {
                    int[] imageSize = getTextureSize(source);
                    fixedSize = new float[]{(float) imageSize[0] / resolution.width, (float) imageSize[1] / resolution.height};
                }

                //add the texture on the map
                textureMap.put(name, new TextureData(id, fixedSize));
            }
        }

        for (Map.Entry<String, CompoundTag> entry : texturesTemp.entrySet())
            data.add(entry.getValue());

        textureOffset += data.size();
        texturesNbt.put("src", src);
        texturesNbt.put("data", data);
    }

    private void parseElements(BlockbenchModel.Element[] elements) {
        for (BlockbenchModel.Element element : elements) {
            if (element.type == null)
                element.type = "cube";
            if (!element.type.equalsIgnoreCase("cube") && !element.type.equalsIgnoreCase("mesh"))
                continue;
            if (element.export != null && !element.export)
                continue;

            //temp variables
            String id = element.uuid;
            CompoundTag nbt = new CompoundTag();

            //parse fields
            nbt.putString("name", element.name);

            //parse transform data
            if (notZero(element.from))
                nbt.put("f", toNbtList(element.from));
            if (notZero(element.to))
                nbt.put("t", toNbtList(element.to));
            if (notZero(element.rotation))
                nbt.put("rot", toNbtList(element.rotation));
            if (notZero(element.origin))
                nbt.put("piv", toNbtList(element.origin));
            if (element.inflate != 0f)
                nbt.putFloat("inf", element.inflate);

            nbt.putBoolean("vsb", element.visibility == null || element.visibility);

            //parse faces
            CompoundTag data;
            if (element.type.equalsIgnoreCase("cube")) {
                data = parseCubeFaces(element.faces);
                nbt.put("cube_data", data);
            } else {
                data = parseMesh(element.faces, element.vertices, element.origin);
                nbt.put("mesh_data", data);
            }


            elementMap.put(id, nbt);
        }
    }

    private CompoundTag parseCubeFaces(JsonObject faces) {
        CompoundTag nbt = new CompoundTag();

        for (String cubeFace : BlockbenchModel.CubeFace.FACES) {
            if (!faces.has(cubeFace))
                continue;

            //convert face json to java object
            BlockbenchModel.CubeFace face = GSON.fromJson(faces.getAsJsonObject(cubeFace), BlockbenchModel.CubeFace.class);

            //dont add null faces
            if (face.texture == null)
                continue;

            //parse texture
            TextureData texture = textureMap.get(textureIdMap.get(face.texture));
            if (texture == null)
                continue;

            CompoundTag faceNbt = new CompoundTag();
            faceNbt.putInt("tex", texture.id);

            //parse face
            if (face.rotation != 0f)
                faceNbt.putFloat("rot", face.rotation);

            //parse uv
            if (notZero(face.uv)) {
                float[] uv = {face.uv[0] * texture.fixedSize[0], face.uv[1] * texture.fixedSize[1], face.uv[2] * texture.fixedSize[0], face.uv[3] * texture.fixedSize[1]};
                faceNbt.put("uv", toNbtList(uv));
            }

            nbt.put(String.valueOf(cubeFace.charAt(0)), faceNbt);
        }

        return nbt;
    }

    private CompoundTag parseMesh(JsonObject faces, JsonObject vertices, float[] offset) {
        CompoundTag nbt = new CompoundTag();

        //parse vertices first, as the faces will reference it later
        //we are going to save them in a String -> Integer map
        //the map will be preserved since it is very common to meshes share the same vertices,
        //so we can reduce even more file size
        HashMap<String, Integer> verticesMap = new HashMap<>();
        ListTag verticesList = new ListTag();

        int index = 0;
        for (Map.Entry<String, JsonElement> entry : vertices.entrySet()) {
            verticesMap.put(entry.getKey(), index);
            float[] arr = jsonToFloat(entry.getValue().getAsJsonArray());
            verticesList.add(FloatTag.valueOf(arr[0] + offset[0]));
            verticesList.add(FloatTag.valueOf(arr[1] + offset[1]));
            verticesList.add(FloatTag.valueOf(arr[2] + offset[2]));
            index++;
        }

        //parse faces
        ListTag texesList = new ListTag();
        ListTag uvsList = new ListTag();
        ListTag facesList = new ListTag();

        int bestType = 0; //byte
        if (index > 255) bestType = 1; //short
        if (index > 32767) bestType = 2; //int
        for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
            //convert json to java object
            BlockbenchModel.MeshFace face = GSON.fromJson(entry.getValue(), BlockbenchModel.MeshFace.class);

            //dont parse empty faces
            //Also skip faces that have less than 3 or more than 4 vertices, since blockbench is jank as hell
            if (face.texture == null || face.vertices == null || face.uv == null || face.vertices.length < 3 || face.vertices.length > 4)
                continue;

            //parse texture
            TextureData texture = textureMap.get(textureIdMap.get(face.texture));
            if (texture == null)
                continue;

            //To get the texture id, shift right 4, to get the vertex count, bitmask with 0xf
            //This just stores both pieces of info in one number, to hopefully save some file size
            short k = (short) ((texture.id << 4) + face.vertices.length);
            texesList.add(ShortTag.valueOf(k));

            if (face.vertices.length > 3)
                reorderVertices(face.vertices, verticesMap, verticesList);

            for (String vertex : face.vertices) {
                //Face indices
                Tag bestVal = switch (bestType) {
                    case 0 -> ByteTag.valueOf(verticesMap.get(vertex).byteValue());
                    case 1 -> ShortTag.valueOf(verticesMap.get(vertex).shortValue());
                    case 2 -> IntTag.valueOf(verticesMap.get(vertex));
                    default -> throw new IllegalStateException("Unexpected value: " + bestType);
                };
                facesList.add(bestVal);

                //UVs
                float[] uv = jsonToFloat(face.uv.getAsJsonArray(vertex));
                float u = uv[0] * texture.fixedSize[0];
                float v = uv[1] * texture.fixedSize[1];
                uvsList.add(FloatTag.valueOf(u));
                uvsList.add(FloatTag.valueOf(v));
            }
        }

        nbt.put("vtx", verticesList);
        nbt.put("tex", texesList);
        nbt.put("fac", facesList);
        nbt.put("uvs", uvsList);
        return nbt;
    }

    private static final FiguraVec3
            v1 = FiguraVec3.of(),
            v2 = FiguraVec3.of(),
            v3 = FiguraVec3.of(),
            v4 = FiguraVec3.of();

    private static void reorderVertices(String[] vertexNames, Map<String, Integer> nameToIndex, ListTag vertices) {
        //Fill in v1, v2, v3, v4 from the given vertices
        readVectors(vertexNames, nameToIndex, vertices);

        if (testOppositeSides(v2, v3, v1, v4)) {
            String temp = vertexNames[2];
            vertexNames[2] = vertexNames[1];
            vertexNames[1] = vertexNames[0];
            vertexNames[0] = temp;
        } else if (testOppositeSides(v1, v2, v3, v4)) {
            String temp = vertexNames[2];
            vertexNames[2] = vertexNames[1];
            vertexNames[1] = temp;
        }

    }

    private static void readVectors(String[] vertexNames, Map<String, Integer> nameToIndex, ListTag vertices) {
        int i = nameToIndex.get(vertexNames[0]);
        v1.set(vertices.getFloat(3*i), vertices.getFloat(3*i+1), vertices.getFloat(3*i+2));
        i = nameToIndex.get(vertexNames[1]);
        v2.set(vertices.getFloat(3*i), vertices.getFloat(3*i+1), vertices.getFloat(3*i+2));
        i = nameToIndex.get(vertexNames[2]);
        v3.set(vertices.getFloat(3*i), vertices.getFloat(3*i+1), vertices.getFloat(3*i+2));
        i = nameToIndex.get(vertexNames[3]);
        v4.set(vertices.getFloat(3 * i), vertices.getFloat(3 * i + 1), vertices.getFloat(3 * i + 2));
    }

    private static final FiguraVec3
            t1 = FiguraVec3.of(),
            t2 = FiguraVec3.of(),
            t3 = FiguraVec3.of(),
            t4 = FiguraVec3.of();

    /**
     * Checks whether the two points given are on opposite sides of the line given.
     */
    private static boolean testOppositeSides(FiguraVec3 linePoint1, FiguraVec3 linePoint2, FiguraVec3 point1, FiguraVec3 point2) {
        t1.set(linePoint1);
        t2.set(linePoint2);
        t3.set(point1);
        t4.set(point2);

        t2.subtract(t1);
        t3.subtract(t1);
        t4.subtract(t1);

        t1.set(t2);
        t1.cross(t3);
        t2.cross(t4);
        return t1.dot(t2) < 0;
    }

    private void parseAnimations(List<CompoundTag> list, BlockbenchModel.Animation[] animations, String modelName, String folders) {
        if (animations == null)
            return;

        int i = 0;
        for (BlockbenchModel.Animation animation : animations) {
            CompoundTag animNbt = new CompoundTag();

            //animation metadata
            animNbt.putString("mdl", folders.isBlank() ? modelName : folders + modelName);
            animNbt.putString("name", animation.name);
            if (!animation.loop.equals("once"))
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

                ListTag effectData = new ListTag();
                ListTag rotData = new ListTag();
                ListTag posData = new ListTag();
                ListTag scaleData = new ListTag();

                //parse keyframes
                JsonObject animationData = entry.getValue().getAsJsonObject();
                for (JsonElement keyframeJson : animationData.get("keyframes").getAsJsonArray()) {
                    BlockbenchModel.KeyFrame keyFrame = GSON.fromJson(keyframeJson, BlockbenchModel.KeyFrame.class);

                    CompoundTag keyframeNbt = new CompoundTag();
                    keyframeNbt.putFloat("time", keyFrame.time);

                    if (effect) {
                        if (!keyFrame.channel.equalsIgnoreCase("timeline"))
                            continue;

                        keyframeNbt.putString("src", keyFrame.data_points.get(0).getAsJsonObject().get("script").getAsString());
                        effectData.add(keyframeNbt);
                    } else {
                        keyframeNbt.putString("int", keyFrame.interpolation);

                        //pre
                        JsonObject dataPoints = keyFrame.data_points.get(0).getAsJsonObject();
                        keyframeNbt.put("pre", parseKeyFrameData(dataPoints, keyFrame.channel));

                        //end
                        if (keyFrame.data_points.size() > 1) {
                            JsonObject endDataPoints = keyFrame.data_points.get(1).getAsJsonObject();
                            keyframeNbt.put("end", parseKeyFrameData(endDataPoints, keyFrame.channel));
                        }

                        //bezier stuff
                        if (notZero(keyFrame.bezier_left_value))
                            keyframeNbt.put("bl", toNbtList(keyFrame.bezier_left_value));
                        if (notZero(keyFrame.bezier_right_value))
                            keyframeNbt.put("br", toNbtList(keyFrame.bezier_right_value));
                        if (isDifferent(keyFrame.bezier_left_time, -0.1f))
                            keyframeNbt.put("blt", toNbtList(keyFrame.bezier_left_time));
                        if (isDifferent(keyFrame.bezier_right_time, 0.1f))
                            keyframeNbt.put("brt", toNbtList(keyFrame.bezier_right_time));

                        switch (keyFrame.channel) {
                            case "position" -> posData.add(keyframeNbt);
                            case "rotation" -> rotData.add(keyframeNbt);
                            case "scale" -> scaleData.add(keyframeNbt);
                        }
                    }
                }

                //add to nbt
                if (effect) {
                    animNbt.put("code", effectData);
                } else {
                    ListTag partAnimations = animationMap.containsKey(id) ? animationMap.get(id) : new ListTag();
                    CompoundTag nbt = new CompoundTag();
                    CompoundTag channels = new CompoundTag();

                    if (!rotData.isEmpty()) {
                        JsonElement globalRotJson = animationData.get("rotation_global");
                        if (globalRotJson != null && globalRotJson.getAsBoolean())
                            channels.put("grot", rotData);
                        else
                            channels.put("rot", rotData);
                    }
                    if (!posData.isEmpty())
                        channels.put("pos", posData);
                    if (!scaleData.isEmpty())
                        channels.put("scl", scaleData);

                    if (!channels.isEmpty()) {
                        nbt.putInt("id", i + animationOffset);
                        nbt.put("data", channels);
                    }
                    if (!nbt.isEmpty())
                        partAnimations.add(nbt);

                    if (!partAnimations.isEmpty())
                        animationMap.put(id, partAnimations);
                }
            }

            list.add(animNbt);
            i++;
        }

        animationOffset += list.size();
    }

    private ListTag parseKeyFrameData(JsonObject object, String channel) {
        BlockbenchModel.KeyFrameData frameData = GSON.fromJson(object, BlockbenchModel.KeyFrameData.class);

        float fallback = channel.equals("scale") ? 1f : 0f;
        Object x = keyFrameData(frameData.x, fallback);
        Object y = keyFrameData(frameData.y, fallback);
        Object z = keyFrameData(frameData.z, fallback);

        ListTag nbt = new ListTag();
        if (x instanceof Float xx && y instanceof Float yy && z instanceof Float zz) {
            nbt.add(FloatTag.valueOf(xx));
            nbt.add(FloatTag.valueOf(yy));
            nbt.add(FloatTag.valueOf(zz));
        } else {
            nbt.add(StringTag.valueOf(String.valueOf(x)));
            nbt.add(StringTag.valueOf(String.valueOf(y)));
            nbt.add(StringTag.valueOf(String.valueOf(z)));
        }

        return nbt;
    }

    private ListTag parseOutliner(JsonArray outliner, boolean parentVsb) {
        ListTag children = new ListTag();

        if (outliner == null)
            return children;

        for (JsonElement element : outliner) {
            //check if it is an ID first
            if (element instanceof JsonPrimitive) {
                String key = element.getAsString();
                if (elementMap.containsKey(key)) {
                    CompoundTag elementNbt = elementMap.get(key);
                    //fix children visibility (very jank)
                    if (elementNbt.contains("vsb") && elementNbt.getBoolean("vsb") == parentVsb)
                        elementNbt.remove("vsb");
                    children.add(elementNbt);
                }

                continue;
            }

            //then parse as GroupElement (outliner)
            CompoundTag groupNbt = new CompoundTag();
            BlockbenchModel.GroupElement group = GSON.fromJson(element, BlockbenchModel.GroupElement.class);

            //skip not exported groups
            if (group.export != null && !group.export)
                continue;

            //parse fields
            groupNbt.putString("name", group.name);

            //visibility
            boolean thisVisibility = group.visibility == null || group.visibility;
            if (thisVisibility != parentVsb)
                groupNbt.putBoolean("vsb", thisVisibility);

            //parse transforms
            if (notZero(group.origin))
                groupNbt.put("piv", toNbtList(group.origin));
            if (notZero(group.rotation))
                groupNbt.put("rot", toNbtList(group.rotation));

            //parent type
            parseParent(group.name, groupNbt);

            //parse children
            if (!(group.children == null || group.children.isEmpty()))
                groupNbt.put("chld", parseOutliner(group.children, thisVisibility));

            //add animations
            if (animationMap.containsKey(group.uuid))
                groupNbt.put("anim", animationMap.get(group.uuid));

            children.add(groupNbt);
        }

        return children;
    }

    // -- helper functions -- //

    //converts a float array into a nbt list
    public static ListTag toNbtList(float[] floats) {
        ListTag list = new ListTag();

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
                case 0 -> list.add(ByteTag.valueOf((byte) f));
                case 1 -> list.add(ShortTag.valueOf((short) f));
                case 2 -> list.add(FloatTag.valueOf(f));
            }
        }

        return list;
    }

    //check if a float array is not composed of only zeros
    public static boolean notZero(float[] floats) {
        return isDifferent(floats, 0f);
    }

    public static boolean isDifferent(float[] floats, float value) {
        if (floats == null)
            return false;

        for (float f : floats) {
            if (f != value) {
                return true;
            }
        }

        return false;
    }

    //try converting a String to float, with a fallback
    public static float toFloat(String input, float fallback) {
        try {
            return Float.parseFloat(input);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static Object keyFrameData(String input, float fallback) {
        try {
            return Float.parseFloat(input);
        } catch (Exception ignored) {
            return input == null || input.isBlank() ? fallback : input;
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

    public static float[] jsonToFloat(JsonArray array) {
        float[] f = new float[array.size()];

        int i = 0;
        for (JsonElement element : array) {
            f[i] = element.isJsonNull() ? 0f : element.getAsFloat();
            i++;
        }

        return f;
    }

    //dummy texture data
    private record TextureData(int id, float[] fixedSize) {}

    //dummy class containing the return object of the parser
    public record ModelData(CompoundTag textures, List<CompoundTag> animationList, CompoundTag modelNbt) {}
}
