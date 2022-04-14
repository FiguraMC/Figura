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

        //add and parse the outliner
        nbt.put("mdl", parseOutliner(gson, model.outliner, elements));

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
            //temp variables
            String id = element.uuid;
            String type = String.valueOf(element.type.charAt(0));
            NbtCompound nbt = new NbtCompound();

            //parse fields
            nbt.putString("name", element.name);
            nbt.putString("type", type);

            //parse transform data
            if (element.from != null && notZero(element.from))
                nbt.put("f", toNbtList(element.from));
            if (element.to != null && notZero(element.to))
                nbt.put("t", toNbtList(element.to));
            if (element.rotation != null && notZero(element.rotation))
                nbt.put("r", toNbtList(element.rotation));
            if (element.origin != null && notZero(element.origin))
                nbt.put("p", toNbtList(element.origin));
            if (element.inflate != 0f)
                nbt.putFloat("i", element.inflate);

            if (element.visibility != null && !element.visibility)
                nbt.putBoolean("v", false);

            //parse faces
            NbtCompound data;
            if (type.equalsIgnoreCase("c")) {
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
                faceNbt.putFloat("r", face.rotation);
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
                faceNbt.put("v", faceVertices);
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

        nbt.put("v", verticesList);
        nbt.put("f", facesList);
        return nbt;
    }

    private static NbtList parseOutliner(Gson gson, JsonArray outliner, HashMap<String, NbtCompound> elements) {
        NbtList children = new NbtList();

        if (outliner == null)
            return children;

        for (JsonElement element : outliner) {
            //check if it is an ID first
            if (element instanceof JsonPrimitive) {
                children.add(elements.get(element.getAsString()));
                continue;
            }

            //then parse as GroupElement (outliner)
            NbtCompound groupNbt = new NbtCompound();
            BlockbenchModel.GroupElement group = gson.fromJson(element, BlockbenchModel.GroupElement.class);

            //parse fields
            groupNbt.putString("name", group.name);
            if (group.visibility != null && !group.visibility)
                groupNbt.putBoolean("v", false);

            //parse transforms
            if (group.origin != null && notZero(group.origin))
                groupNbt.put("p", toNbtList(group.origin));

            //parse children
            if (group.children != null && group.children.size() > 0)
                groupNbt.put("chd", parseOutliner(gson, group.children, elements));

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
}
