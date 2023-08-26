package org.figuramc.figura.parsers.Buwwet;

import com.google.gson.*;
import net.minecraft.nbt.*;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.luaj.vm2.ast.Str;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/// A collection of all the recoverable BlockBench information from a FiguraModel
public class BlockBenchPart {
    public String name;
    public String uuid;
    public float[] origin;
    public float[] rotation;

    // Unimplemented and unimportant.
    public int color = 0;
    public  Boolean visibility = true;
    public  Boolean locked = false;

    /// Fill out all the shared fields that Groups and Elements share, and generate a random uuid.
    public BlockBenchPart(CompoundTag nbt) {
        if (!nbt.contains("name")) {
            FiguraMod.LOGGER.error("Invalid block bench part.");
        }
        // Assign values shared by all: name and uuid
        name = nbt.get("name").getAsString();
        uuid = UUID.randomUUID().toString();

        origin = fillVectorIfNone(nbt.get("piv"), 3);
        rotation = fillVectorIfNone(nbt.get("rot"), 3);


        FiguraMod.LOGGER.info(nbt.getAllKeys().toString());
        //FiguraMod.LOGGER.info(this.toString());

    }
    // TODO: might move these helper functions somewhere else
    // We have to re-add these vectors because FiguraModel removes them if they're all zeros.
    public static float[] fillVectorIfNone(Tag value, int length) {
        if (value != null) {
            // Get all the values of the array/vector
            ListTag arrayTag = (ListTag) value;

            float[] vector = new float[arrayTag.size()];
            int index = 0;
            for (Tag v : arrayTag) {
                NumericTag f = (NumericTag) v;
                vector[index] = f.getAsFloat();
                index++;
            }
            return vector;
        } else {
            // Return an empty vector.
            float[] vector = new float[length];
            Arrays.fill(vector, 0);
            return vector;
        }
    }

    public static JsonArray floatArrayToJson(float[] array) {
        JsonArray json = new JsonArray();

        for (int i = 0; i < array.length; i++) {
            json.add(array[i]);
        }

        return json;
    }

    public static void appendJsonArrayToJsonArray(JsonArray target, JsonArray appender) {
        for (JsonElement element : appender) {
            target.add(element);
        }
    }
    // parse the "elements" of the BlockBench model
    public static JsonArray parseAsElementList(BlockBenchPart part) {
        JsonArray element_array = new JsonArray();

        if (part instanceof Group) {
            // Become recursive.
            for (BlockBenchPart child : ((Group) part).children) {
                // Append all the children's children's to our element_array
                JsonArray children_json = parseAsElementList(child);
                appendJsonArrayToJsonArray(element_array, children_json);
            }
        } else if (part instanceof Element) {
            // Process the element and add it to the list.
            JsonObject element_json = ((Element) part).toJson();
            element_array.add(element_json);
        }

        return element_array;
    }

    // Iterates through the FiguraModel's model tag.
    public static BlockBenchPart parseNBTchildren(CompoundTag nbt) {

        // Check if this element has children of their own (is a group).
        if (nbt.contains("chld")) {
            // We're a group
            // TODO: group constructor
            Group elementGroup = new Group(nbt);

            List<BlockBenchPart> children = new ArrayList<BlockBenchPart>();

            ListTag nbtChildren = (ListTag) nbt.get("chld");
            for (Tag childNbtRaw: nbtChildren) {
                CompoundTag nbtChild = (CompoundTag) childNbtRaw;
                // Process the child and add it if it's valid.
                BlockBenchPart child = parseNBTchildren(nbtChild);
                if (child != null) {
                    children.add(child);
                }

                //FiguraMod.LOGGER.info(String.valueOf(nbtChild));
            }
            // Move it to an array
            BlockBenchPart[] childrenArray = new BlockBenchPart[children.size()];
            elementGroup.children = children.toArray(childrenArray);

            return elementGroup;
        } else {
            // Are we an element then?
            if (nbt.contains("cube_data") || nbt.contains("mesh_data")) {
                return new Element(nbt);
            }
        }
        return null;

    }
    public static class Group extends BlockBenchPart {
        public BlockBenchPart[] children;

        public Boolean isOpen = true;

        public Group(CompoundTag nbt) {
            super(nbt);
        }

        @Override
        public String toString() {
            return "Group: {name: " + name + ", uuid: " + uuid + ", piv: " + origin.toString() + "}";
        }
    }
    public static class Element extends BlockBenchPart {
        // Either mesh or cube.4
        public String type;

        public float inflate = 0;

        // CubeData is simply an array of the faces
        public FiguraModelParser.CubeData cubeData;


        // Mesh data contains an array for the vertices and another for the faces.
        public FiguraModelParser.MeshData meshData;

        public Element(CompoundTag nbt) {
            super(nbt);
            // Get from and to.


            if (nbt.contains("inf")) {
                NumericTag inf = (NumericTag) nbt.get("inf");
                this.inflate = inf.getAsFloat();
            }


            // Check if we're a mesh or a cube
            if (nbt.contains("cube_data")) {
                this.type = "cube";
                this.cubeData = new FiguraModelParser.CubeData(nbt);
            } else if (nbt.contains("mesh_data")) {
                this.type = "mesh";
                //FiguraMod.LOGGER.info(nbt.get("mesh_data").getAsString());
                this.meshData = FiguraModelParser.MeshData.generateFromElement(nbt);
            } else {
                FiguraMod.LOGGER.error("Element is neither a mesh or cube.");
            }
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            // Basic.
            json.addProperty("name", this.name);
            json.addProperty("uuid", this.uuid);
            json.addProperty("type", this.type);
            json.addProperty("color", this.color);

            json.add("origin", BlockBenchPart.floatArrayToJson(this.origin));
            json.add("rotation", BlockBenchPart.floatArrayToJson(this.rotation));

            // Add type specific stuff.
            if (this.type == "cube") {
                json.add("from", BlockBenchPart.floatArrayToJson(this.cubeData.from));
                json.add("to", BlockBenchPart.floatArrayToJson(this.cubeData.to));
                json.add("faces", this.cubeData.facesToJson());

            } else if (this.type == "mesh") {
                json.add("vertices", this.meshData.verticesToJson());
                json.add("faces", this.meshData.facesToJson());
            }

            return json;
        }

        @Override
        public String toString() {
            return "Element: {name: " + name + ", uuid: " + uuid + ", piv: " + origin.toString() + "}";
        }
    }
}
