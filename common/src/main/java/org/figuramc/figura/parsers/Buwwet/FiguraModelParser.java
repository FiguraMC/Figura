package org.figuramc.figura.parsers.Buwwet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.*;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPartReader;
import org.figuramc.figura.model.rendering.AvatarRenderer;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.figuramc.figura.utils.IOUtils;
import org.luaj.vm2.ast.Str;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static org.figuramc.figura.parsers.Buwwet.BlockBenchPart.fillVectorIfNone;
import static org.figuramc.figura.parsers.Buwwet.BlockBenchPart.floatArrayToJson;

/// Parses Figura models into blockbench models by performing all of the calculations already done previously but on reverse.
public class FiguraModelParser {

    public static Path getDownloaderAvatarDirectory() {
        Path avatar_path = IOUtils.getOrCreateDir(FiguraMod.getFiguraDirectory(), "avatars");
        return IOUtils.getOrCreateDir(avatar_path, "downloaded");
    }

    public static void parseAvatar(CompoundTag nbt) {
        // metadata
        CompoundTag metadataNbt = nbt.getCompound("metadata");
        String avatarName = metadataNbt.getString("name");
        String avatarAuthor = metadataNbt.getString("authors");

        // Save location
        Path avatarSavePath = IOUtils.getOrCreateDir(getDownloaderAvatarDirectory(), avatarName);

        // Parse textures and save them, also generate a json object to be included in all BlockBench models
        ArrayList<TextureData> textures = TextureData.fromAvatarTexturesNbt(nbt.getCompound("textures"));
        JsonArray jsonModelTextures = new JsonArray();
        // id, width and height
        HashMap<Integer, Integer[]> textureSize = new HashMap<>();
        for (TextureData texture : textures) {



            // Save the image!
            try {
                // We do not have to do anything fancy as the byte[] contains headers (aka not raw)
                OutputStream new_texture = new FileOutputStream(avatarSavePath.resolve(texture.name).toString(), false);
                new_texture.write(texture.textureBytes);
                new_texture.flush();
                new_texture.close();

                // Get width and height of texture.
                InputStream in = new ByteArrayInputStream(texture.textureBytes);
                BufferedImage buf = ImageIO.read(in);

                textureSize.put(texture.id, new Integer[] {buf.getWidth(), buf.getHeight()});

                //FiguraMod.LOGGER.info("Texture " + texture.id + ": " +  buf.getWidth() + ", " + buf.getHeight());
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to save texture: " + e);
            }

            // Add to the json array
            jsonModelTextures.add(texture.toBlockBenchTextureJson());
        }

        // Model Parser
        // All models are clumped together at "models.MODEL_HERE", they require to be given their own separate file.
        for (Tag model_tag: nbt.getCompound("models").getList("chld", Tag.TAG_COMPOUND)) {
            CompoundTag model = (CompoundTag) model_tag;
            JsonObject modelJson = new JsonObject();
            modelJson.addProperty("name", model.get("name").getAsString());
            // Parse the figura model to our own types
            BlockBenchPart rootFiguraModel = BlockBenchPart.parseNBTchildren(model, textureSize);
            // Get the elements list
            JsonArray elementsJson = BlockBenchPart.parseAsElementList(rootFiguraModel);
            modelJson.add("elements", elementsJson);
            // Get the outliner
            JsonObject outlinerJson = BlockBenchPart.Group.toJsonOutliner(rootFiguraModel).getAsJsonObject();
            modelJson.add("outliner", outlinerJson.get("children"));   // Get children to make the outliner an array, (also model shouldn't even be referenced in blockbench)
            // Add the meta data
            JsonObject metaJson = new JsonObject();
            metaJson.addProperty("format_version", "4.5");
            metaJson.addProperty("model_format", "free");
            metaJson.addProperty("box_uv", false); //no please
            modelJson.add("meta", metaJson);

            // Add the resolution
            // TODO: can figura models set this? should there be a check?
            JsonObject resolutionJson = new JsonObject();
            resolutionJson.addProperty("width", 64);
            resolutionJson.addProperty("height", 64);
            modelJson.add("resolution", resolutionJson);

            // Add textures
            modelJson.add("textures", jsonModelTextures);

            // Animations
            ArrayList<CompoundTag> modelAnimRaw = getModelAnimations(nbt, model.getString("name"));
            // init the arrays of each animation.
            HashMap<Integer, ArrayList<Pair<String, JsonElement>>> animatorArray = new HashMap<>();
            for (int i = 0; i < modelAnimRaw.size(); i++) {
                //FiguraMod.LOGGER.info("anims: " + i);
                animatorArray.put(i, new ArrayList<>());

                // Check if they have any code.
                if (modelAnimRaw.get(i).contains("code")) {
                    JsonObject codeAnimator = FiguraAnimationParser.AnimatorGroupData.animatorFromCode(modelAnimRaw.get(i));
                    animatorArray.get(i).add(new Pair<>("effects", codeAnimator));
                }
            }
            // Get all the animators.
            if (rootFiguraModel instanceof BlockBenchPart.Group) {
                ((BlockBenchPart.Group) rootFiguraModel).getAnimators(animatorArray);
            }
            JsonArray animations = FiguraAnimationParser.createJsonAnimations(animatorArray, modelAnimRaw);
            modelJson.add("animations", animations);

            //modelJson.add("animations", new JsonArray());

            try {
                FileWriter modelFile = new FileWriter(avatarSavePath.resolve(model.get("name").getAsString() + ".bbmodel").toString(), false);
                modelFile.write(modelJson.toString());
                modelFile.flush();
                modelFile.close();
            } catch (IOException e) {
                FiguraMod.LOGGER.error("Error while saving to file a model: " + e);
                //throw new RuntimeException(e);
            }
            //FiguraMod.LOGGER.info(modelJson.toString());
        }

        // Parse all the scripts
        CompoundTag scriptsNbt = nbt.getCompound("scripts");
        for (String scriptName : scriptsNbt.getAllKeys()) {
            // Transform the bytes to a string
            byte[] luaFileBytes = scriptsNbt.getByteArray(scriptName);
            String luaScript = new String(luaFileBytes, StandardCharsets.UTF_8);

            try {
                FileWriter modelFile = new FileWriter(avatarSavePath.resolve(scriptName + ".lua").toString(), false);
                modelFile.write(luaScript);
                modelFile.flush();
                modelFile.close();
            } catch (IOException e) {
                FiguraMod.LOGGER.error("Error while saving to file lua script: " + e);
                //throw new RuntimeException(e);
            }
        };

        // Save an avatar.json
        JsonObject avatarJson = new JsonObject();
        JsonArray avatarAuthors = new JsonArray();
        avatarAuthors.add(avatarAuthor);
        avatarJson.addProperty("name", avatarName);
        avatarJson.add("authors", avatarAuthors);
        try {
            FileWriter avatarFile = new FileWriter(avatarSavePath.resolve("avatar.json").toString(), false);
            avatarFile.write(avatarJson.toString());
            avatarFile.flush();
            avatarFile.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Error while saving avatar.json");
        }



    }
    // Get the animations exclusive to this model
    public static ArrayList<CompoundTag> getModelAnimations(CompoundTag nbtRoot, String model_name) {
        ArrayList<CompoundTag> array = new ArrayList<>();

        for (Tag animRaw : nbtRoot.getList("animations", Tag.TAG_COMPOUND)) {
            CompoundTag anim = (CompoundTag) animRaw;
            if (model_name.equals(anim.getString("mdl"))) {

                array.add(anim);
            }
        }

        return array;
    }

    public static class TextureData {
        public String name;
        public Integer id;
        public String uuid;
        public byte[] textureBytes;

        public TextureData(String name, Integer id, byte[] bytes) {
            this.name = name + ".png";
            this.id = id;
            this.uuid = UUID.randomUUID().toString();
            this.textureBytes = bytes;
        }

        public JsonObject toBlockBenchTextureJson() {
            JsonObject json = new JsonObject();

            // TODO: omitting critical fields maybe.
            json.addProperty("name", this.name);
            json.addProperty("relative_path", "../" + this.name);
            json.addProperty("id", this.id);
            json.addProperty("uuid", this.uuid);

            json.addProperty("saved", true);
            json.addProperty("mode", "bitmap");
            json.addProperty("visible", true);


            return json;
        }

        public static ArrayList<TextureData> fromAvatarTexturesNbt(CompoundTag nbt) {
            ArrayList<TextureData> textureData = new ArrayList<>();
            // Names of textures are stored separately
            //TODO: this id might be wrong!
            int texId = 0;
            for (Tag textureNameNbt : nbt.getList("data", Tag.TAG_COMPOUND)) {
                CompoundTag textureNameCompound = (CompoundTag) textureNameNbt;
                String textureName = textureNameCompound.getString("d");

                // Get the bytes stored in the name of this texture
                byte[] textureBytes = nbt.getCompound("src").getByteArray(textureName);

                textureData.add(new TextureData(textureName, texId, textureBytes));

                texId++;
            }

            return textureData;
        }

    }

    public static class CubeData {
        static final List<String> FACES = List.of("north", "south", "west", "east", "up", "down");

        // CubeData pt.2 (too lazy to move)
        public CubeFaceData[] faces;
        public float[] from;
        public float[] to;

        public class CubeFaceData {

            public String name;
            /// 4-sized length
            public float[] uv;
            public int texture;

            public CubeFaceData(String name, float[] uv, int texture) {
                this.name = name;
                this.uv = uv;
                this.texture = texture;
            }
        }

        // Parse from nbt.
        public CubeData(CompoundTag nbt, HashMap<Integer, Integer[]> textureSize) {
            this.from = fillVectorIfNone(nbt.get("f"), 3);
            this.to = fillVectorIfNone(nbt.get("t"), 3);

            this.faces = generateFiguraFaces(nbt.get("cube_data"), textureSize);
        }

        public JsonObject facesToJson() {
            JsonObject jsonMap = new JsonObject();

            for (CubeFaceData face : this.faces) {
                JsonObject jsonFace = new JsonObject();
                // Create the face in json and then add it to the map.
                jsonFace.addProperty("texture", face.texture);
                jsonFace.add("uv", floatArrayToJson(face.uv));

                jsonMap.add(face.name, jsonFace);
            }

            return jsonMap;
        }

        private CubeFaceData[] generateFiguraFaces(Tag faces, HashMap<Integer, Integer[]> textureSize) {
            CompoundTag facesNBT = (CompoundTag) faces;
            //FiguraMod.LOGGER.info(faces.getAsString());

            // POSSIBLE BLOCKBENCH ERROR: Some elements do not have any faces but have the "cube_data" field.
            if (((CompoundTag) faces).size() == 0) {
                return new CubeFaceData[0];
            }

            ArrayList<CubeFaceData> finalFaces = new ArrayList<>();
            // Figura completely butchers the names of the faces, we need to find them again and put the correct one.
            for (String faceName : CubeData.FACES) {
                CompoundTag faceNbt = (CompoundTag) facesNBT.get(String.valueOf(faceName.charAt(0)));
                // Get the uv and texture index
                int texture = faceNbt.getInt("tex");

                float[] uv = fillVectorIfNone(faceNbt.get("uv"), 4);
                // Divide uvs by two TODO: not right
                //FiguraMod.LOGGER.info("texture id: " + texture + ", width: " + textureSize.get(texture)[0] + ", Face uv mult:" + (textureSize.get(texture)[0] / 64 * 4));
                uv[0] = uv[0] * 64 / textureSize.get(texture)[0];
                uv[1] = uv[1] * 64 / textureSize.get(texture)[1];
                uv[2] = uv[2] * 64 / textureSize.get(texture)[0];
                uv[3] = uv[3] * 64 / textureSize.get(texture)[1];


                finalFaces.add(new CubeFaceData(faceName, uv, texture));
                //FiguraMod.LOGGER.info(faceName);
            }

            CubeFaceData[] array = new CubeFaceData[finalFaces.size()];
            finalFaces.toArray(array);

            return array;
        }
    }

    public static class MeshData {
        public HashMap<String, float[]> vertices;
        public MeshFaceData[] faces;

        public MeshData(HashMap<String, float[]> vertices, MeshFaceData[] faces) {
            this.vertices = vertices;
            this.faces = faces;
        }
        // Returns the "vertices" json object for mesh elements
        public JsonObject verticesToJson() {
            JsonObject jsonMap = new JsonObject();

            for (Map.Entry<String, float[]> vertex: this.vertices.entrySet()) {
                jsonMap.add(vertex.getKey(), floatArrayToJson(vertex.getValue()));
            }

            return jsonMap;
        }

        public JsonObject facesToJson() {
            JsonObject jsonMap = new JsonObject();

            int faceId = 0;
            for (MeshFaceData face : this.faces) {
                JsonObject faceJson = new JsonObject();
                JsonObject faceUvMap = new JsonObject();
                JsonArray faceVerts = new JsonArray();

                // Add the uvs
                for (Map.Entry<String, float[]> uvVector : face.uv.entrySet()) {
                    faceUvMap.add(uvVector.getKey(), floatArrayToJson(uvVector.getValue()));
                }
                // Add the texture

                // Add the verts
                for (String vert : face.vertices) {
                    faceVerts.add(vert);
                }

                faceJson.add("uv", faceUvMap);
                faceJson.add("vertices", faceVerts);

                faceJson.addProperty("texture", face.texture);

                // POSSIBLE ERROR FOR BLOCKBENCH: probs expects a name with letters
                jsonMap.add(String.valueOf(faceId), faceJson);
                faceId++;
            }

            return jsonMap;
        }

        public static class MeshFaceData {
            // uvs are sized 2
            public HashMap<String, float[]> uv;
            public String[] vertices;

            public int texture;

            public MeshFaceData(ArrayList<Pair<String, Vertex>> vertices, int texture, HashMap<Integer, Integer[]> textureSize) {
                ArrayList<String> new_verticies = new ArrayList<>();
                HashMap<String, float[]> new_uv = new HashMap<>();

                this.texture = texture;

                for (Pair<String, Vertex> v : vertices) {
                    // add this vertex's name to the list of vertices in the face
                    new_verticies.add(v.getFirst().toString());

                    // Add the uv data of each vertex
                    float[] uvs = new float[] {
                            v.getSecond().u / (textureSize.get(texture)[0] / 64),
                            v.getSecond().v / (textureSize.get(texture)[1] / 64)
                    };
                    new_uv.put(v.getFirst(), uvs);
                }
                String[] vertex_array = new String[new_verticies.size()];
                new_verticies.toArray(vertex_array);
                this.vertices = vertex_array;
                this.uv = new_uv;
            }
        }

        public static MeshData generateFromElement(CompoundTag element, float[] origin, HashMap<Integer, Integer[]> textureSize) {
            // Holds all the positions of the vertices
            HashMap<String, float[]> dataVertices = new HashMap<>();
            // Holds the face data
            ArrayList<MeshFaceData> faceData = new ArrayList<>();

            // Lists all the vectors by their face and texture.
            // Integer is the texture
            // String is the vertex id
            ArrayList<Pair<Integer, ArrayList<Pair<String, Vertex>>>> faces = new ArrayList<>();
            // Read the vertices of the mesh
            readMesh(element, faces);

            for (Pair<Integer, ArrayList<Pair<String, Vertex>>> face : faces) {
                //FiguraMod.LOGGER.info(String.valueOf(entry.getKey()));

                // Save the vertices
                for (Pair<String, Vertex> v : face.getSecond()) {
                    //FiguraMod.LOGGER.info(v.x + ", " + v.y + ", " + v.z);

                    // Store all the positions of the vertices with their unique name (for the mesh at least)
                    Vertex vertex = v.getSecond();
                    // Subtract the origin to fix height
                    dataVertices.put(v.getFirst(), new float[] {vertex.x - origin[0], vertex.y - origin[1], vertex.z - origin[2]});
                }
                // Create the face data
                faceData.add(new MeshFaceData(face.getSecond(), face.getFirst(), textureSize));
            }

            MeshFaceData[] facesArray = new MeshFaceData[faceData.size()];
            faceData.toArray(facesArray);
            return new MeshData(dataVertices, facesArray);
        }

        // stolen straight from FiguraModelPartReader, because it doesn't include the face.
        public static void readMesh(CompoundTag data, ArrayList<Pair<Integer, ArrayList<Pair<String, Vertex>>>> faces) {
            CompoundTag meshData = data.getCompound("mesh_data");
            // mesh_data:
            // "vtx": List<Float>, xyz
            // "tex": List<Short>, (texID << 4) + numVerticesInFace
            // "fac": List<Byte, Short, or Int>, just the indices of various vertices
            // "uvs": List<Float>, uv for each vertex

            // Get the vertex, UV, and texture lists from the mesh data
            ListTag verts = meshData.getList("vtx", Tag.TAG_FLOAT);
            ListTag uvs = meshData.getList("uvs", Tag.TAG_FLOAT);
            ListTag tex = meshData.getList("tex", Tag.TAG_SHORT);

            // Determine the best data type to use for the face list based on the size of the vertex list
            int bestType = 0; // byte
            if (verts.size() > 255 * 3) bestType = 1; // short
            if (verts.size() > 32767 * 3) bestType = 2; // int

            // Get the face list using the determined data type
            ListTag fac = switch (bestType) {
                case 0 -> meshData.getList("fac", Tag.TAG_BYTE);
                case 1 -> meshData.getList("fac", Tag.TAG_SHORT);
                default -> meshData.getList("fac", Tag.TAG_INT);
            };

            // Initialize counters for the vertex and UV lists
            int vi = 0, uvi = 0;

            // Create arrays to store temporary vertex and UV data
            float[] posArr = new float[12];
            float[] uvArr = new float[8];

            // faces
            ArrayList<Pair<String, Vertex>> verticeBuffer = new ArrayList<>();

            // Iterate through the texture list
            for (int ti = 0; ti < tex.size(); ti++) {
                // Get the packed texture data for this iteration
                short packed = tex.getShort(ti);
                // Extract the texture ID and number of vertices from the packed data
                int texId = packed >> 4;
                int numVerts = packed & 0xf;

                //FiguraMod.LOGGER.info("new face " + String.valueOf(vi) + ", texid: " + texId);

                // Extract the vertex and UV data for the current texture
                for (int j = 0; j < numVerts; j++) {
                    // Get the vertex ID based on the determined data type
                    int vid = switch (bestType) {
                        case 0 -> ((ByteTag) fac.get(vi + j)).getAsByte() & 0xff;
                        case 1 -> fac.getShort(vi + j) & 0xffff;
                        default -> fac.getInt(vi + j);
                    };

                    // Get the vertex position and UV data from the lists
                    posArr[3 * j] = verts.getFloat(3 * vid);
                    posArr[3 * j + 1] = verts.getFloat(3 * vid + 1);
                    posArr[3 * j + 2] = verts.getFloat(3 * vid + 2);

                    uvArr[2 * j] = uvs.getFloat(uvi + 2 * j);
                    uvArr[2 * j + 1] = uvs.getFloat(uvi + 2 * j + 1);
                }

                // Calculate the normal vector for the current texture
                FiguraVec3 p1 = FiguraVec3.of(posArr[0], posArr[1], posArr[2]);
                FiguraVec3 p2 = FiguraVec3.of(posArr[3], posArr[4], posArr[5]);
                FiguraVec3 p3 = FiguraVec3.of(posArr[6], posArr[7], posArr[8]);
                p3.subtract(p2);
                p1.subtract(p2);
                p3.cross(p1);
                p3.normalize();
                // p3 now contains the normal vector

                // Add the vertex data to the appropriate builder
                for (int j = 0; j < numVerts; j++) {
                    verticeBuffer.add(new Pair<>(String.valueOf(vi + j), new Vertex(
                            posArr[3 * j], posArr[3 * j + 1], posArr[3 * j + 2],
                            uvArr[2 * j], uvArr[2 * j + 1],
                            (float) p3.x, (float) p3.y, (float) p3.z
                    )));
                }
                // Add a vertex if necessary
                if (numVerts == 3) {
                    // TODO: might cause problems. Check later
                    verticeBuffer.add(new Pair<>("extra", new Vertex(
                            posArr[6], posArr[7], posArr[8],
                            uvArr[4], uvArr[5],
                            (float) p3.x, (float) p3.y, (float) p3.z
                    )));
                }

                // Increment the counters for the vertex and UV lists
                vi += numVerts;
                uvi += 2 * numVerts;

                // Clear the vertex buffer.
                faces.add(new Pair<>(texId, (ArrayList<Pair<String, Vertex>>) verticeBuffer.clone()));
                verticeBuffer.clear();
            }
        }
    }
}
