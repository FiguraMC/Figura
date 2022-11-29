package org.moon.figura.parsers;

import com.google.gson.*;
import net.minecraft.nbt.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.ParentType;
import org.moon.figura.utils.IOUtils;

import java.io.File;
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
    public ModelData parseModel(Path avatarFolder, File sourceFile, String json, String modelName, String folders) {
        //parse json -> object
        BlockbenchModel model = GSON.fromJson(json, BlockbenchModel.class);

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
        nbt.put("chld", parseOutliner(model.outliner, null));

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

    private void parseTextures(Path avatar, File sourceFile, String folders, String modelName, CompoundTag texturesNbt, BlockbenchModel.Texture[] textures, BlockbenchModel.Resolution resolution) {
        if (textures == null)
            return;

        String pathRegex = Pattern.quote(avatar + File.separator);

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
            //name
            String name = folders + textures[i].name;
            if (name.endsWith(".png"))
                name = name.substring(0, name.length() - 4);

            //render type
            String renderType = textures[i].render_mode;
            if (name.endsWith("_e")) {
                renderType = "emissive";
                name = name.substring(0, name.length() - 2);
            }
            if (!renderType.equals("emissive"))
                renderType = "default";

            //parse the texture data
            String path;
            byte[] source;
            try {
                //check the file to load
                Path p = sourceFile.toPath().resolve(textures[i].relative_path);
                File f = p.toFile();
                if (!f.exists()) throw new Exception("File do not exists!");
                if (!p.normalize().startsWith(avatar)) throw new Exception("File from outside the avatar folder!");

                //load texture
                source = IOUtils.readFileBytes(f);
                path = f.getCanonicalPath()
                        .replaceFirst(pathRegex, "")
                        .replaceAll("[/\\\\]", ".");
                path = path.substring(0, path.length() - 4);

                //feedback
                FiguraMod.debug("Loaded" + (renderType.equals("emissive") ? " Emissive" : "") + " Texture \"{}\" from {}", name, f);
            } catch (Exception ignored) {
                //otherwise, load from the source stored in the model
                source = Base64.getDecoder().decode(textures[i].source.substring("data:image/png;base64,".length()));
                path = folders + modelName + "." + name + (renderType.equals("emissive") ? "_e" : "");
            }

            //add source nbt
            src.putByteArray(path, source);

            //add textures nbt
            if (texturesTemp.containsKey(name)) {
                texturesTemp.get(name).putString(renderType, path);
            } else {
                //create nbt
                CompoundTag compound = new CompoundTag();
                compound.putString(renderType, path);

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
                int[] imageSize = getTextureSize(source);
                float[] fixedSize = new float[]{(float) imageSize[0] / resolution.width, (float) imageSize[1] / resolution.height};

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

            //temp variables
            String id = element.uuid;
            CompoundTag nbt = new CompoundTag();

            //parse fields
            nbt.putString("name", element.name);

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
            if (face.uv != null && notZero(face.uv)) {
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
            JsonArray arr = entry.getValue().getAsJsonArray();
            verticesList.add(FloatTag.valueOf(arr.get(0).getAsFloat()+offset[0]));
            verticesList.add(FloatTag.valueOf(arr.get(1).getAsFloat()+offset[1]));
            verticesList.add(FloatTag.valueOf(arr.get(2).getAsFloat()+offset[2]));
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
                JsonArray uv = face.uv.getAsJsonArray(vertex);
                float u = uv.get(0).getAsFloat() * texture.fixedSize[0];
                float v = uv.get(1).getAsFloat() * texture.fixedSize[1];
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
        BlockbenchModel.KeyFrameData endFrameData = GSON.fromJson(object, BlockbenchModel.KeyFrameData.class);

        float fallback = channel.equals("scale") ? 1f : 0f;
        float x = toFloat(endFrameData.x, fallback);
        float y = toFloat(endFrameData.y, fallback);
        float z = toFloat(endFrameData.z, fallback);

        if (channel.equals("position")) {
            x = -x;
        } else if (channel.equals("rotation")) {
            x = -x;
            y = -y;
        }

        ListTag nbt = new ListTag();
        nbt.add(FloatTag.valueOf(x));
        nbt.add(FloatTag.valueOf(y));
        nbt.add(FloatTag.valueOf(z));

        return nbt;
    }

    private ListTag parseOutliner(JsonArray outliner, Boolean parentVsb) {
        ListTag children = new ListTag();

        if (outliner == null)
            return children;

        for (JsonElement element : outliner) {
            //check if it is an ID first
            if (element instanceof JsonPrimitive) {
                if (elementMap.containsKey(element.getAsString())) {
                    CompoundTag elementNbt = elementMap.get(element.getAsString());

                    //fix children visibility (very jank)
                    if (parentVsb != null && elementNbt.contains("vsb") && elementNbt.getBoolean("vsb") == parentVsb)
                        elementNbt.remove("vsb");

                    children.add(elementNbt);
                }

                continue;
            }

            //then parse as GroupElement (outliner)
            CompoundTag groupNbt = new CompoundTag();
            BlockbenchModel.GroupElement group = GSON.fromJson(element, BlockbenchModel.GroupElement.class);

            //parse fields
            groupNbt.putString("name", group.name);

            //visibility
            if (group.visibility != null && !group.visibility && (parentVsb == null || parentVsb))
                groupNbt.putBoolean("vsb", false);

            //parse transforms
            if (group.origin != null && notZero(group.origin))
                groupNbt.put("piv", toNbtList(group.origin));
            if (group.rotation != null && notZero(group.rotation))
                groupNbt.put("rot", toNbtList(group.rotation));

            //parent type
            parseParent(group.name, groupNbt);

            //parse children
            if (group.children != null && group.children.size() > 0)
                groupNbt.put("chld", parseOutliner(group.children, group.visibility));

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
    public record ModelData(CompoundTag textures, List<CompoundTag> animationList, CompoundTag modelNbt) {}
}
