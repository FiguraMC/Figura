// Class Version: 17
package org.figuramc.figura.parsers.Buwwet;

import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import java.util.Iterator;
import java.util.Arrays;
import net.minecraft.nbt.*;
import java.util.UUID;
import org.figuramc.figura.FiguraMod;

public class BlockBenchPart
{
    public String name;
    public String uuid;
    public float[] origin;
    public float[] rotation;
    public int color;
    public Boolean visibility;
    public Boolean locked;
    
    public BlockBenchPart(final Tag CompoundTag) {
        this.color = 0;
        this.visibility = true;
        this.locked = false;
        if (!CompoundTag.contains("name")) {
            FiguraMod.LOGGER.error("Invalid block bench part.");
        }
        this.name = CompoundTag.get("name").asString();
        this.uuid = UUID.randomUUID().toString();
        this.origin = fillVectorIfNone(CompoundTag.get("piv"), 3);
        this.rotation = fillVectorIfNone(CompoundTag.get("rot"), 3);
        if (CompoundTag.contains("vsb")) {
            this.visibility = CompoundTag.getBoolean("vsb");
        }
    }
    
    public static float[] fillVectorIfNone(final Tag Tag, final int n) {
        if (Tag != null) {
            final NbtList list = (NbtList)Tag;
            final float[] array = new float[list.size()];
            int n2 = 0;
            final Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                array[n2] = ((AbstractNbtNumber)iterator.next()).floatValue();
                ++n2;
            }
            return array;
        }
        final float[] array2 = new float[n];
        Arrays.fill(array2, 0.0f);
        return array2;
    }
    
    public static JsonArray floatArrayToJson(final float[] array) {
        final JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < array.length; ++i) {
            jsonArray.add((Number)array[i]);
        }
        return jsonArray;
    }
    
    public static void appendJsonArrayToJsonArray(final JsonArray jsonArray, final JsonArray jsonArray2) {
        final Iterator iterator = jsonArray2.iterator();
        while (iterator.hasNext()) {
            jsonArray.add((JsonElement)iterator.next());
        }
    }
    
    public static JsonArray parseAsElementList(final BlockBenchPart blockBenchPart) {
        final JsonArray jsonArray = new JsonArray();
        if (blockBenchPart instanceof final Group group) {
            if (group.children != null) {
                final BlockBenchPart[] children = group.children;
                for (int length = children.length, i = 0; i < length; ++i) {
                    appendJsonArrayToJsonArray(jsonArray, parseAsElementList(children[i]));
                }
            }
        }
        else if (blockBenchPart instanceof final Element element) {
            jsonArray.add((JsonElement)element.toJson());
        }
        return jsonArray;
    }
    
    public static BlockBenchPart parseNBTchildren(final CompoundTag CompoundTag, final HashMap<Integer, Integer[]> hashMap) {
        if (CompoundTag.contains("chld")) {
            final Group group = new Group(CompoundTag);
            final ArrayList list = new ArrayList();
            final Iterator iterator = ((NbtList)CompoundTag.get("chld")).iterator();
            while (iterator.hasNext()) {
                final BlockBenchPart nbTchildren = parseNBTchildren((CompoundTag)iterator.next(), hashMap);
                if (nbTchildren != null) {
                    list.add(nbTchildren);
                }
            }
            group.children = (BlockBenchPart[])list.toArray(new BlockBenchPart[list.size()]);
            return group;
        }
        if (CompoundTag.contains("cube_data") || CompoundTag.contains("mesh_data")) {
            final Element element = new Element(CompoundTag, hashMap);
            if (element.type == null) {
                return null;
            }
            return element;
        }
        else {
            if (CompoundTag.contains("name")) {
                return new Group(CompoundTag);
            }
            return null;
        }
    }
    
    public static class Group extends BlockBenchPart
    {
        public BlockBenchPart[] children;
        public ArrayList<FiguraAnimationParser.AnimatorGroupData> animationAnimators;
        
        public Group(final CompoundTag CompoundTag) {
            super(CompoundTag);
            if (CompoundTag.contains("anim")) {
                this.animationAnimators = FiguraAnimationParser.AnimatorGroupData.parseNbt(CompoundTag);
            }
        }
        
        public void getAnimators(final HashMap<Integer, ArrayList<Pair<String, JsonElement>>> hashMap) {
            if (this.animationAnimators != null) {
                for (FiguraAnimationParser.AnimatorGroupData animatorGroupData : this.animationAnimators) {
                    final ArrayList list = hashMap.get(animatorGroupData.animation_id);
                    if (list == null) {
                        FiguraMod.LOGGER.error("Has an animator for an invalid animation id: " + animatorGroupData.animation_id);
                    }
                    else {
                        final JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("name", this.name);
                        jsonObject.addProperty("type", "bone");
                        jsonObject.add("keyframes", (JsonElement)animatorGroupData.toKeyframesJson());
                        list.add(new Pair((Object)this.uuid, (Object)jsonObject));
                    }
                }
            }
            if (this.children != null) {
                for (final BlockBenchPart blockBenchPart : this.children) {
                    if (blockBenchPart instanceof final Group group) {
                        group.getAnimators(hashMap);
                    }
                }
            }
        }
        
        public static JsonElement toJsonOutliner(final BlockBenchPart blockBenchPart) {
            if (blockBenchPart instanceof Element) {
                return (JsonElement)new JsonPrimitive(blockBenchPart.uuid);
            }
            if (blockBenchPart instanceof Group) {
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", blockBenchPart.name);
                jsonObject.addProperty("uuid", blockBenchPart.uuid);
                jsonObject.addProperty("color", (Number)blockBenchPart.color);
                jsonObject.addProperty("visibility", blockBenchPart.visibility);
                jsonObject.add("rotation", (JsonElement)BlockBenchPart.floatArrayToJson(blockBenchPart.rotation));
                jsonObject.add("origin", (JsonElement)BlockBenchPart.floatArrayToJson(blockBenchPart.origin));
                jsonObject.addProperty("isOpen", Boolean.valueOf(false));
                jsonObject.addProperty("locked", Boolean.valueOf(false));
                jsonObject.addProperty("export", Boolean.valueOf(true));
                jsonObject.addProperty("autouv", (Number)0);
                jsonObject.addProperty("mirror_uv", (Number)0);
                final JsonArray jsonArray = new JsonArray();
                if (((Group)blockBenchPart).children != null) {
                    final BlockBenchPart[] children = ((Group)blockBenchPart).children;
                    for (int length = children.length, i = 0; i < length; ++i) {
                        jsonArray.add(toJsonOutliner(children[i]));
                    }
                }
                jsonObject.add("children", (JsonElement)jsonArray);
                return (JsonElement)jsonObject;
            }
            FiguraMod.LOGGER.error("Failed to identify root part!");
            return null;
        }
        
        public void getModelUsedTextures(final ArrayList<Integer> list) {
            if (this.children == null) {
                return;
            }
            for (final BlockBenchPart blockBenchPart : this.children) {
                if (blockBenchPart instanceof final Group group) {
                    group.getModelUsedTextures(list);
                }
                if (blockBenchPart instanceof final Element element) {
                    for (final Integer n : element.getTexture()) {
                        if (!list.contains(n)) {
                            list.add(n);
                        }
                    }
                }
            }
        }
        
        public void updateElementBlockBenchTexture(final ArrayList<Integer> list) {
            if (this.children == null) {
                return;
            }
            for (final BlockBenchPart blockBenchPart : this.children) {
                if (blockBenchPart instanceof final Group group) {
                    group.updateElementBlockBenchTexture(list);
                }
                if (blockBenchPart instanceof final Element element) {
                    element.matchSetTexture(list);
                }
            }
        }
        
        @Override
        public String toString() {
            return "Group: {name: " + this.name + ", uuid: " + this.uuid + ", piv: " + this.origin.toString();
        }
    }
    
    public static class Element extends BlockBenchPart
    {
        public String type;
        public float inflate;
        public FiguraModelParser.CubeData cubeData;
        public FiguraModelParser.MeshData meshData;
        
        public Element(final CompoundTag CompoundTag, final HashMap<Integer, Integer[]> hashMap) {
            super(CompoundTag);
            this.inflate = 0.0f;
            if (CompoundTag.contains("inf")) {
                this.inflate = ((AbstractNbtNumber)CompoundTag.get("inf")).floatValue();
            }
            if (CompoundTag.contains("cube_data")) {
                this.type = "cube";
                this.cubeData = new FiguraModelParser.CubeData(CompoundTag, hashMap);
            }
            if (CompoundTag.contains("mesh_data")) {
                this.type = "mesh";
                this.meshData = FiguraModelParser.MeshData.generateFromElement(CompoundTag, this.origin, hashMap);
            }
        }
        
        @Nullable
        public ArrayList<Integer> getTexture() {
            final ArrayList list = new ArrayList();
            if (this.cubeData != null) {
                for (final FiguraModelParser.CubeData.CubeFaceData cubeFaceData : this.cubeData.faces) {
                    if (cubeFaceData.texture != null) {
                        list.add(cubeFaceData.texture);
                    }
                }
            }
            else if (this.meshData != null) {
                final FiguraModelParser.MeshData.MeshFaceData[] faces2 = this.meshData.faces;
                for (int length2 = faces2.length, j = 0; j < length2; ++j) {
                    list.add(faces2[j].texture);
                }
            }
            return list;
        }
        
        public void matchSetTexture(final ArrayList<Integer> list) {
            if (this.cubeData != null) {
                for (final FiguraModelParser.CubeData.CubeFaceData cubeFaceData : this.cubeData.faces) {
                    cubeFaceData.texture = list.indexOf(cubeFaceData.texture);
                }
            }
            if (this.meshData != null) {
                for (final FiguraModelParser.MeshData.MeshFaceData meshFaceData : this.meshData.faces) {
                    meshFaceData.texture = list.indexOf(meshFaceData.texture);
                }
            }
        }
        
        public JsonObject toJson() {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", this.name);
            jsonObject.addProperty("uuid", this.uuid);
            jsonObject.addProperty("type", this.type);
            jsonObject.addProperty("color", (Number)this.color);
            jsonObject.addProperty("inflate", (Number)this.inflate);
            jsonObject.addProperty("locked", Boolean.valueOf(false));
            jsonObject.add("origin", (JsonElement)BlockBenchPart.floatArrayToJson(this.origin));
            jsonObject.add("rotation", (JsonElement)BlockBenchPart.floatArrayToJson(this.rotation));
            if (this.type == "cube") {
                jsonObject.addProperty("autouv", (Number)0);
                jsonObject.addProperty("box_uv", Boolean.valueOf(false));
                jsonObject.addProperty("rescale", Boolean.valueOf(false));
                jsonObject.add("from", (JsonElement)BlockBenchPart.floatArrayToJson(this.cubeData.from));
                jsonObject.add("to", (JsonElement)BlockBenchPart.floatArrayToJson(this.cubeData.to));
                jsonObject.add("faces", (JsonElement)this.cubeData.facesToJson());
            }
            else if (this.type == "mesh") {
                jsonObject.add("vertices", (JsonElement)this.meshData.verticesToJson());
                jsonObject.add("faces", (JsonElement)this.meshData.facesToJson());
            }
            return jsonObject;
        }
        
        @Override
        public String toString() {
            return "Element: {name: " + this.name + ", uuid: " + this.uuid + ", piv: " + this.origin.toString();
        }
    }
}
