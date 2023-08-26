package org.figuramc.figura.parsers.Buwwet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPartReader;
import org.figuramc.figura.model.rendering.AvatarRenderer;
import org.figuramc.figura.model.rendering.Vertex;
import org.figuramc.figura.model.rendering.texture.FiguraTexture;
import org.figuramc.figura.model.rendering.texture.FiguraTextureSet;
import org.luaj.vm2.ast.Str;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.figuramc.figura.parsers.Buwwet.BlockBenchPart.fillVectorIfNone;
import static org.figuramc.figura.parsers.Buwwet.BlockBenchPart.floatArrayToJson;

/// Parses Figura models into blockbench models by performing all of the calculations already done previously but on reverse.
public class FiguraModelParser {

    public static void parseAvatar(CompoundTag nbt) {
        // Get textures (required for some vector parsers).
        CompoundTag texturesNbt = nbt.getCompound("textures");

        //ListTag texturesList = texturesNbt.getList("data", Tag.TAG_COMPOUND);
        // All models are clumped together at "models.MODEL_HERE", they require to be given their own separate file.
        for (Tag model_tag: nbt.getCompound("models").getList("chld", Tag.TAG_COMPOUND)) {
            CompoundTag model = (CompoundTag) model_tag;
            JsonObject modelJson = new JsonObject();
            modelJson.addProperty("name", model.get("name").getAsString());
            // Parse the figura model to our own types
            BlockBenchPart rootFiguraModel = BlockBenchPart.parseNBTchildren(model);
            // Get the elements list
            JsonArray elementsJson = BlockBenchPart.parseAsElementList(rootFiguraModel);
            modelJson.add("elements", elementsJson);
            // Get the outliner
            JsonObject outlinerJson = BlockBenchPart.Group.toJsonOutliner(rootFiguraModel).getAsJsonObject();
            modelJson.add("outliner", outlinerJson.get("children"));   // Get children to make the outliner an array, (also model shouldn't even be referenced in blockbench)

            JsonObject metaJson = new JsonObject();
            metaJson.addProperty("format_version", "4.5");
            metaJson.addProperty("model_format", "modded_entity");
            metaJson.addProperty("box_uv", "true");
            modelJson.add("meta", metaJson);

            FiguraMod.LOGGER.info(modelJson.toString());
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
        public CubeData(CompoundTag nbt) {
            this.from = fillVectorIfNone(nbt.get("f"), 3);
            this.to = fillVectorIfNone(nbt.get("t"), 3);

            this.faces = generateFiguraFaces(nbt.get("cube_data"));
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

        private CubeFaceData[] generateFiguraFaces(Tag faces) {
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
                float[] uv = fillVectorIfNone(faceNbt.get("uv"), 4);
                int texture = facesNBT.getInt("tex");

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
                // Add the verts
                for (String vert : face.vertices) {
                    faceVerts.add(vert);
                }

                faceJson.add("uv", faceUvMap);
                faceJson.add("vertices", faceVerts);

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

            public int texture = 0;

            public MeshFaceData(ArrayList<Pair<String, Vertex>> vertices, int texture) {
                ArrayList<String> new_verticies = new ArrayList<>();
                HashMap<String, float[]> new_uv = new HashMap<>();

                this.texture = texture;

                for (Pair<String, Vertex> v : vertices) {
                    // add this vertex's name to the list of vertices in the face
                    new_verticies.add(v.getFirst().toString());

                    // Add the uv data of each vertex
                    // TODO: fix uvs by dividing by FiguraVec3 uvFixer = FiguraVec3.of();`
                    float[] uvs = new float[] {v.getSecond().u / 2, v.getSecond().v / 2};
                    new_uv.put(v.getFirst(), uvs);
                }
                String[] vertex_array = new String[new_verticies.size()];
                new_verticies.toArray(vertex_array);
                this.vertices = vertex_array;
                this.uv = new_uv;
            }
        }

        public static MeshData generateFromElement(CompoundTag element, float[] origin) {
            List<Integer> facesByTexture = new ArrayList<>();
            // Temp because we still aren't catching textures
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);



            // Holds all the positions of the vertices
            HashMap<String, float[]> dataVertices = new HashMap<>();
            // Holds the face data
            ArrayList<MeshFaceData> faceData = new ArrayList<>();

            // Lists all the vectors by their face and texture.
            // Integer is the texture
            // String is the vertex id
            ArrayList<Pair<Integer, ArrayList<Pair<String, Vertex>>>> faces = new ArrayList<>();
            // Read the vertices of the mesh
            readMesh(facesByTexture, element, faces);

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
                faceData.add(new MeshFaceData(face.getSecond(), face.getFirst()));
            }

            MeshFaceData[] facesArray = new MeshFaceData[faceData.size()];
            faceData.toArray(facesArray);
            return new MeshData(dataVertices, facesArray);
        }

        // stolen straight from FiguraModelPartReader, because it doesn't include the face.
        public static void readMesh(List<Integer> facesByTexture, CompoundTag data, ArrayList<Pair<Integer, ArrayList<Pair<String, Vertex>>>> faces) {
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
                // Increment the number of faces for the current texture ID
                facesByTexture.set(texId, facesByTexture.get(texId) + 1);

                FiguraMod.LOGGER.info("new face " + String.valueOf(vi) + ", texid: " + texId);

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
