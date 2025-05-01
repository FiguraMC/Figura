// Class Version: 17
package org.figuramc.figura.parsers.Buwwet;

//import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.*;
import org.figuramc.figura.math.vector.FiguraVec3;
//import net.minecraft.nbt.ByteTag;
import org.figuramc.figura.model.rendering.Vertex;
import java.util.Map;
import java.util.UUID;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.charset.StandardCharsets;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Collections;
//import net.minecraft.nbt.Tag;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
//import net.minecraft.nbt.CompoundTag;
import org.figuramc.figura.utils.IOUtils;
import org.figuramc.figura.FiguraMod;
import java.nio.file.Path;

public class FiguraModelParser
{
    public static Path getDownloaderAvatarDirectory() {
        return IOUtils.getOrCreateDir(IOUtils.getOrCreateDir(FiguraMod.getFiguraDirectory(), "avatars"), "downloaded");
    }
    
    public static String parseAvatar(final CompoundTag CompoundTag) {
        final CompoundTag compound = CompoundTag.getCompound("metadata");
        final String string = compound.getString("name");
        final String string2 = compound.getString("authors");
        final Path orCreateDir = IOUtils.getOrCreateDir(getDownloaderAvatarDirectory(), string);
        final ArrayList<TextureData> fromAvatarTexturesNbt = TextureData.fromAvatarTexturesNbt(CompoundTag.getCompound("textures"));
        final HashMap hashMap = new HashMap();
        for (TextureData textureData : fromAvatarTexturesNbt) {
            try {
                final FileOutputStream fileOutputStream = new FileOutputStream(orCreateDir.resolve(textureData.name).toString(), false);
                fileOutputStream.write(textureData.textureBytes);
                fileOutputStream.flush();
                fileOutputStream.close();
                final BufferedImage read = ImageIO.read(new ByteArrayInputStream(textureData.textureBytes));
                if (read == null) {
                    FiguraMod.LOGGER.error("Error loading texture; Buffer is null for texture " + textureData.name);
                }
                hashMap.put(textureData.id, new Integer[] { read.getWidth(), read.getHeight() });
            }
            catch (final Exception ex) {
                FiguraMod.LOGGER.error("Failed to save texture: " + ex);
            }
        }
        final ArrayList<TextureData> emisiveTexturesFromNbt = TextureData.emisiveTexturesFromNbt(CompoundTag.getCompound("textures"));
        for (TextureData textureData2 : emisiveTexturesFromNbt) {
            try {
                final FileOutputStream fileOutputStream2 = new FileOutputStream(orCreateDir.resolve(textureData2.name).toString(), false);
                fileOutputStream2.write(textureData2.textureBytes);
                fileOutputStream2.flush();
                fileOutputStream2.close();
            }
            catch (final Exception ex2) {
                FiguraMod.LOGGER.error("Failed to save emissive texture: " + ex2);
            }
        }
        final JsonObject jsonObject = new JsonObject();
        final JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("format_version", "4.5");
        jsonObject2.addProperty("model_format", "free");
        jsonObject2.addProperty("box_uv", Boolean.valueOf(false));
        jsonObject.add("meta", (JsonElement)jsonObject2);
        final JsonObject jsonObject3 = new JsonObject();
        jsonObject3.addProperty("width", (Number)64);
        jsonObject3.addProperty("height", (Number)64);
        jsonObject.add("resolution", (JsonElement)jsonObject3);
        jsonObject.add("elements", (JsonElement)new JsonArray());
        final JsonArray jsonArray = new JsonArray();
        final ArrayList list = new ArrayList();
        for (final TextureData textureData3 : fromAvatarTexturesNbt) {
            if (!list.contains(textureData3.name)) {
                jsonArray.add((JsonElement)textureData3.toBlockBenchTextureJson());
                list.add(textureData3.name);
            }
        }
        for (final TextureData textureData4 : emisiveTexturesFromNbt) {
            if (!list.contains(textureData4.name)) {
                jsonArray.add((JsonElement)textureData4.toBlockBenchTextureJson());
                list.add(textureData4.name);
            }
        }
        jsonObject.add("textures", (JsonElement)jsonArray);
        try {
            final FileWriter fileWriter = new FileWriter(orCreateDir.resolve("buwwetTextures.bbmodel").toString(), false);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (final IOException ex3) {
            FiguraMod.LOGGER.error("Error while saving to file a model: " + ex3);
        }
        for (CompoundTag CompoundTag2 : CompoundTag.getCompound("models").getList("chld", 10)) {
            final JsonObject jsonObject4 = new JsonObject();
            jsonObject4.addProperty("name", CompoundTag2.get("name").asString());
            final BlockBenchPart.Group group = (BlockBenchPart.Group)BlockBenchPart.parseNBTchildren(CompoundTag2, hashMap);
            jsonObject4.add("outliner", BlockBenchPart.Group.toJsonOutliner(group).getAsJsonObject().get("children"));
            final JsonObject jsonObject5 = new JsonObject();
            jsonObject5.addProperty("format_version", "4.5");
            jsonObject5.addProperty("model_format", "free");
            jsonObject5.addProperty("box_uv", Boolean.valueOf(false));
            jsonObject4.add("meta", (JsonElement)jsonObject5);
            final JsonObject jsonObject6 = new JsonObject();
            jsonObject6.addProperty("width", (Number)64);
            jsonObject6.addProperty("height", (Number)64);
            jsonObject4.add("resolution", (JsonElement)jsonObject6);
            final JsonArray jsonArray2 = new JsonArray();
            final ArrayList list2 = new ArrayList<Comparable>();
            group.getModelUsedTextures(list2);
            Collections.sort((List<Comparable>)list2);
            group.updateElementBlockBenchTexture(list2);
            for (final TextureData textureData5 : fromAvatarTexturesNbt) {
                if (list2.contains(textureData5.id)) {
                    jsonArray2.add((JsonElement)textureData5.toBlockBenchTextureJson());
                }
            }
            jsonObject4.add("textures", (JsonElement)jsonArray2);
            jsonObject4.add("elements", (JsonElement)BlockBenchPart.parseAsElementList(group));
            final HashMap<Integer, CompoundTag> modelAnimations = getModelAnimations(CompoundTag, CompoundTag2.getString("name"));
            final int size = CompoundTag.getList("animations", 10).size();
            final HashMap<Integer, ArrayList<Pair>> hashMap2 = new HashMap<Integer, ArrayList<Pair>>();
            for (int i = 0; i < size; ++i) {
                hashMap2.put(i, new ArrayList<Pair>());
                if (modelAnimations.containsKey(i) && ((CompoundTag)modelAnimations.get(i)).contains("code")) {
                    hashMap2.get(i).add(new Pair((Object)"effects", (Object)FiguraAnimationParser.AnimatorGroupData.animatorFromCode((CompoundTag)modelAnimations.get(i))));
                }
            }
            FiguraMod.LOGGER.info("Parsed the animations of the avatar.");
            if (group instanceof BlockBenchPart.Group) {
                group.getAnimators((HashMap<Integer, ArrayList<Pair<String, JsonElement>>>)hashMap2);
            }
            jsonObject4.add("animations", (JsonElement)FiguraAnimationParser.createJsonAnimations((HashMap<Integer, ArrayList<Pair<String, JsonElement>>>)hashMap2, modelAnimations));
            try {
                final FileWriter fileWriter2 = new FileWriter(orCreateDir.resolve(CompoundTag2.get("name").asString() + ".bbmodel").toString(), false);
                fileWriter2.write(jsonObject4.toString());
                fileWriter2.flush();
                fileWriter2.close();
            }
            catch (final IOException ex4) {
                FiguraMod.LOGGER.error("Error while saving to file a model: " + ex4);
            }
        }
        final CompoundTag compound2 = CompoundTag.getCompound("scripts");
        for (String s : compound2.getKeys()) {
            final String s2 = new String(compound2.getByteArray(s), StandardCharsets.UTF_8);
            final String replace = s.replace(".", "/");
            if (s.contains(".")) {
                final String substring = replace.substring(0, replace.lastIndexOf("/"));
                try {
                    Files.createDirectories(orCreateDir.resolve(substring), (FileAttribute<?>[])new FileAttribute[0]);
                }
                catch (final IOException ex5) {
                    FiguraMod.LOGGER.error("Failed to create folder for lua script.");
                }
            }
            try {
                final FileWriter fileWriter3 = new FileWriter(orCreateDir.resolve(replace + ".lua").toString(), false);
                fileWriter3.write(s2);
                fileWriter3.flush();
                fileWriter3.close();
            }
            catch (final IOException ex6) {
                FiguraMod.LOGGER.error("Error while saving to file lua script: " + ex6);
            }
        }
        if (CompoundTag.contains("sounds")) {
            final CompoundTag compound3 = CompoundTag.getCompound("sounds");
            for (String s3 : compound3.getKeys()) {
                try {
                    final FileOutputStream fileOutputStream3 = new FileOutputStream(orCreateDir.resolve(s3 + ".ogg").toString(), false);
                    fileOutputStream3.write(compound3.getByteArray(s3));
                    fileOutputStream3.flush();
                    fileOutputStream3.close();
                }
                catch (final Exception ex7) {
                    FiguraMod.LOGGER.error("Failed to save sound " + s3);
                }
            }
        }
        final JsonObject jsonObject7 = new JsonObject();
        final JsonArray jsonArray3 = new JsonArray();
        jsonArray3.add(string2);
        jsonObject7.addProperty("name", string);
        jsonObject7.add("authors", (JsonElement)jsonArray3);
        try {
            final FileWriter fileWriter4 = new FileWriter(orCreateDir.resolve("avatar.json").toString(), false);
            fileWriter4.write(jsonObject7.toString());
            fileWriter4.flush();
            fileWriter4.close();
        }
        catch (final Exception ex8) {
            FiguraMod.LOGGER.error("Error while saving avatar.json");
        }
        return string;
    }
    
    public static HashMap<Integer, CompoundTag> getModelAnimations(final CompoundTag CompoundTag, final String s) {
        final HashMap hashMap = new HashMap();
        int n = 0;
        for (final CompoundTag CompoundTag2 : CompoundTag.getList("animations", 10)) {
            if (s.equals(CompoundTag2.getString("mdl"))) {
                hashMap.put(n, CompoundTag2);
            }
            ++n;
        }
        return hashMap;
    }
    
    public static class TextureData
    {
        public String name;
        public Integer id;
        public String uuid;
        public byte[] textureBytes;
        
        public TextureData(final String s, final Integer id, final byte[] textureBytes) {
            this.name = s + ".png";
            this.id = id;
            this.uuid = UUID.randomUUID().toString();
            this.textureBytes = textureBytes;
        }
        
        public JsonObject toBlockBenchTextureJson() {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", this.name);
            jsonObject.addProperty("relative_path", "../" + this.name);
            jsonObject.addProperty("id", (Number)this.id);
            jsonObject.addProperty("uuid", this.uuid);
            jsonObject.addProperty("saved", Boolean.valueOf(true));
            jsonObject.addProperty("mode", "bitmap");
            jsonObject.addProperty("visible", Boolean.valueOf(true));
            return jsonObject;
        }
        
        public static ArrayList<TextureData> emisiveTexturesFromNbt(final CompoundTag CompoundTag) {
            final ArrayList list = new ArrayList();
            for (final CompoundTag CompoundTag2 : CompoundTag.getList("data", 10)) {
                if (!CompoundTag2.contains("e")) {
                    continue;
                }
                final String string = CompoundTag2.getString("e");
                list.add(new TextureData(string, null, CompoundTag.getCompound("src").getByteArray(string)));
            }
            return list;
        }
        
        public static ArrayList<TextureData> fromAvatarTexturesNbt(final CompoundTag CompoundTag) {
            final ArrayList list = new ArrayList();
            int n = 0;
            for (final CompoundTag CompoundTag2 : CompoundTag.getList("data", 10)) {
                if (!CompoundTag2.contains("d")) {
                    continue;
                }
                final String string = CompoundTag2.getString("d");
                list.add(new TextureData(string, n, CompoundTag.getCompound("src").getByteArray(string)));
                ++n;
            }
            return list;
        }
    }
    
    public static class CubeData
    {
        static final List<String> FACES;
        public CubeFaceData[] faces;
        public float[] from;
        public float[] to;
        
        public CubeData(final CompoundTag CompoundTag, final HashMap<Integer, Integer[]> hashMap) {
            this.from = BlockBenchPart.fillVectorIfNone(CompoundTag.get("f"), 3);
            this.to = BlockBenchPart.fillVectorIfNone(CompoundTag.get("t"), 3);
            this.faces = this.generateFiguraFaces(CompoundTag.get("cube_data"), hashMap);
        }
        
        public JsonObject facesToJson() {
            final JsonObject jsonObject = new JsonObject();
            for (final CubeFaceData cubeFaceData : this.faces) {
                final JsonObject jsonObject2 = new JsonObject();
                if (cubeFaceData.texture != null) {
                    jsonObject2.addProperty("texture", (Number)cubeFaceData.texture);
                }
                jsonObject2.add("uv", (JsonElement)BlockBenchPart.floatArrayToJson(cubeFaceData.uv));
                jsonObject.add(cubeFaceData.name, (JsonElement)jsonObject2);
            }
            return jsonObject;
        }
        
        private CubeFaceData[] generateFiguraFaces(final Tag Tag, final HashMap<Integer, Integer[]> hashMap) {
            final CompoundTag CompoundTag = (CompoundTag)Tag;
            if (((CompoundTag)Tag).getSize() == 0) {
                final ArrayList list = new ArrayList();
                final Iterator<String> iterator = CubeData.FACES.iterator();
                while (iterator.hasNext()) {
                    list.add(new CubeFaceData(iterator.next(), new float[] { 0.0f, 0.0f, 0.0f, 0.0f }, null));
                }
                final CubeFaceData[] array = new CubeFaceData[list.size()];
                list.toArray(array);
                return array;
            }
            final ArrayList list2 = new ArrayList();
            for (final String s : CubeData.FACES) {
                if (CompoundTag.contains(String.valueOf(s.charAt(0)))) {
                    final CompoundTag CompoundTag2 = (CompoundTag)CompoundTag.get(String.valueOf(s.charAt(0)));
                    if (CompoundTag2.getKeys().size() <= 0) {
                        continue;
                    }
                    final int int1 = CompoundTag2.getInt("tex");
                    final float[] fillVectorIfNone = BlockBenchPart.fillVectorIfNone(CompoundTag2.get("uv"), 4);
                    if (hashMap.get(int1) != null && hashMap.get(int1)[0] != 0 && hashMap.get(int1)[1] != 0) {
                        fillVectorIfNone[0] = fillVectorIfNone[0] * 64.0f / hashMap.get(int1)[0];
                        fillVectorIfNone[1] = fillVectorIfNone[1] * 64.0f / hashMap.get(int1)[1];
                        fillVectorIfNone[2] = fillVectorIfNone[2] * 64.0f / hashMap.get(int1)[0];
                        fillVectorIfNone[3] = fillVectorIfNone[3] * 64.0f / hashMap.get(int1)[1];
                    }
                    else {
                        FiguraMod.LOGGER.error("TextureSize requested not found? Cube");
                    }
                    list2.add(new CubeFaceData(s, fillVectorIfNone, int1));
                }
                else {
                    list2.add(new CubeFaceData(s, new float[] { 0.0f, 0.0f, 0.0f, 0.0f }, null));
                }
            }
            final CubeFaceData[] array2 = new CubeFaceData[list2.size()];
            list2.toArray(array2);
            return array2;
        }
        
        static {
            FACES = List.of("north", "south", "west", "east", "up", "down");
        }
        
        public class CubeFaceData
        {
            public String name;
            public float[] uv;
            public Integer texture;
            
            public CubeFaceData(final String name, final float[] uv, final Integer texture) {
                this.name = name;
                this.uv = uv;
                this.texture = texture;
            }
        }
    }
    
    public static class MeshData
    {
        public HashMap<String, float[]> vertices;
        public MeshFaceData[] faces;
        
        public MeshData(final HashMap<String, float[]> vertices, final MeshFaceData[] faces) {
            this.vertices = vertices;
            this.faces = faces;
        }
        
        public JsonObject verticesToJson() {
            final JsonObject jsonObject = new JsonObject();
            for (final Map.Entry entry : this.vertices.entrySet()) {
                jsonObject.add((String)entry.getKey(), (JsonElement)BlockBenchPart.floatArrayToJson((float[])entry.getValue()));
            }
            return jsonObject;
        }
        
        public JsonObject facesToJson() {
            final JsonObject jsonObject = new JsonObject();
            int n = 0;
            for (final MeshFaceData meshFaceData : this.faces) {
                final JsonObject jsonObject2 = new JsonObject();
                final JsonObject jsonObject3 = new JsonObject();
                final JsonArray jsonArray = new JsonArray();
                for (final Map.Entry entry : meshFaceData.uv.entrySet()) {
                    jsonObject3.add((String)entry.getKey(), (JsonElement)BlockBenchPart.floatArrayToJson((float[])entry.getValue()));
                }
                final String[] vertices = meshFaceData.vertices;
                for (int length2 = vertices.length, j = 0; j < length2; ++j) {
                    jsonArray.add(vertices[j]);
                }
                jsonObject2.add("uv", (JsonElement)jsonObject3);
                jsonObject2.add("vertices", (JsonElement)jsonArray);
                jsonObject2.addProperty("texture", (Number)meshFaceData.texture);
                jsonObject.add(String.valueOf(n), (JsonElement)jsonObject2);
                ++n;
            }
            return jsonObject;
        }
        
        public static MeshData generateFromElement(final CompoundTag CompoundTag, final float[] array, final HashMap<Integer, Integer[]> hashMap) {
            final HashMap hashMap2 = new HashMap();
            final ArrayList list = new ArrayList();
            final ArrayList list2 = new ArrayList();
            readMesh(CompoundTag, list2);
            for (final Pair pair : list2) {
                for (final Pair pair2 : (ArrayList)pair.getSecond()) {
                    final Vertex vertex = (Vertex)pair2.getSecond();
                    hashMap2.put(pair2.getFirst(), new float[] { vertex.x - array[0], vertex.y - array[1], vertex.z - array[2] });
                }
                list.add(new MeshFaceData((ArrayList<Pair<String, Vertex>>)pair.getSecond(), (int)pair.getFirst(), hashMap));
            }
            final MeshFaceData[] array2 = new MeshFaceData[list.size()];
            list.toArray(array2);
            return new MeshData(hashMap2, array2);
        }
        
        public static void readMesh(final CompoundTag CompoundTag, final ArrayList<Pair<Integer, ArrayList<Pair<String, Vertex>>>> list) {
            final CompoundTag compound = CompoundTag.getCompound("mesh_data");
            final ListTag list2 = compound.getList("vtx", 5);
            final ListTag list3 = compound.getList("uvs", 5);
            final ListTag list4 = compound.getList("tex", 2);
            int n = 0;
            if (list2.size() > 765) {
                n = 1;
            }
            if (list2.size() > 98301) {
                n = 2;
            }
            final ListTag list6 = switch (n) {
                case 0 -> compound.getList("fac", 1);
                case 1 -> compound.getList("fac", 2);
                default -> compound.getList("fac", 3);
            };
            int n2 = 0;
            int n3 = 0;
            final float[] array = new float[12];
            final float[] array2 = new float[8];
            final ArrayList<Pair> list7 = new ArrayList<Pair>();
            for (int i = 0; i < list4.size(); ++i) {
                final short short1 = list4.getShort(i);
                final int n4 = short1 >> 4;
                final int n5 = short1 & 0xF;
                for (int j = 0; j < n5; ++j) {
                    final int n6 = switch (n) {
                        case 0 -> ((ByteTag)list6.method_10534(n2 + j)).method_10698() & 0xFF;
                        case 1 -> list6.getShort(n2 + j) & 0xFFFF;
                        default -> list6.getInt(n2 + j);
                    };
                    array[3 * j] = list2.getFloat(3 * n6);
                    array[3 * j + 1] = list2.getFloat(3 * n6 + 1);
                    array[3 * j + 2] = list2.getFloat(3 * n6 + 2);
                    array2[2 * j] = list3.getFloat(n3 + 2 * j);
                    array2[2 * j + 1] = list3.getFloat(n3 + 2 * j + 1);
                }
                final FiguraVec3 of = FiguraVec3.of(array[0], array[1], array[2]);
                final FiguraVec3 of2 = FiguraVec3.of(array[3], array[4], array[5]);
                final FiguraVec3 of3 = FiguraVec3.of(array[6], array[7], array[8]);
                of3.subtract(of2);
                of.subtract(of2);
                of3.cross(of);
                of3.normalize();
                for (int k = 0; k < n5; ++k) {
                    list7.add(new Pair((Object)String.valueOf(n2 + k), (Object)new Vertex(array[3 * k], array[3 * k + 1], array[3 * k + 2], array2[2 * k], array2[2 * k + 1], (float)of3.x, (float)of3.y, (float)of3.z)));
                }
                if (n5 == 3) {
                    list7.add(new Pair((Object)"extra", (Object)new Vertex(array[6], array[7], array[8], array2[4], array2[5], (float)of3.x, (float)of3.y, (float)of3.z)));
                }
                n2 += n5;
                n3 += 2 * n5;
                list.add((Pair<Integer, ArrayList<Pair<String, Vertex>>>)new Pair((Object)n4, (Object)list7.clone()));
                list7.clear();
            }
        }
        
        public static class MeshFaceData
        {
            public HashMap<String, float[]> uv;
            public String[] vertices;
            public int texture;
            
            public MeshFaceData(final ArrayList<Pair<String, Vertex>> list, final int texture, final HashMap<Integer, Integer[]> hashMap) {
                final ArrayList list2 = new ArrayList();
                final HashMap uv = new HashMap();
                this.texture = texture;
                for (final Pair pair : list) {
                    list2.add(((String)pair.getFirst()).toString());
                    final float[] array = { ((Vertex)pair.getSecond()).u, ((Vertex)pair.getSecond()).v };
                    if (hashMap.get(texture) != null && hashMap.get(texture)[0] != 0 && hashMap.get(texture)[1] != 0) {
                        array[0] = array[0] * 64.0f / hashMap.get(texture)[0];
                        array[1] = array[1] * 64.0f / hashMap.get(texture)[1];
                    }
                    else {
                        FiguraMod.LOGGER.error("TextureSize requested not found? Mesh");
                    }
                    uv.put(pair.getFirst(), array);
                }
                final String[] vertices = new String[list2.size()];
                list2.toArray(vertices);
                this.vertices = vertices;
                this.uv = uv;
            }
        }
    }
}
