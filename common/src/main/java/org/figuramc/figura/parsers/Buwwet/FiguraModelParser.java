package org.figuramc.figura.parsers.Buwwet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
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

        BlockBenchPart rootFiguraModel = BlockBenchPart.parseNBTchildren(nbt.getCompound("models"));

        JsonArray json = BlockBenchPart.parseAsElementList(rootFiguraModel);

        FiguraMod.LOGGER.info(json.toString());

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
            JsonArray vertices = new JsonArray();

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
                faceJson.add("vertices", faceUvMap);

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

            public MeshFaceData(ArrayList<Pair<Vertex, Integer>> vertices, int texture) {
                ArrayList<String> new_verticies = new ArrayList<>();
                HashMap<String, float[]> new_uv = new HashMap<>();

                this.texture = texture;

                for (Pair<Vertex, Integer> v : vertices) {
                    // add this vertex's name to the list of vertices in the face
                    new_verticies.add(v.getSecond().toString());

                    // Add the uv data of each vertex
                    new_uv.put(v.getSecond().toString(), new float[] {v.getFirst().u, v.getFirst().v});
                }
                String[] vertex_array = new String[new_verticies.size()];
                new_verticies.toArray(vertex_array);
                this.vertices = vertex_array;
                this.uv = new_uv;
            }
        }

        public static MeshData generateFromElement(CompoundTag element) {
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

            Map<Integer, List<Vertex>> vertices = new HashMap<>();
            // Read the vertices of the mesh
            FiguraModelPartReader.readMesh(facesByTexture, element, vertices);


            int vertexId = 0;
            ArrayList<Pair<Vertex, Integer>> vertexFaceBuffer = new ArrayList<>();
            for (Map.Entry<Integer, List<Vertex>> entry : vertices.entrySet()) {
                //FiguraMod.LOGGER.info(String.valueOf(entry.getKey()));

                for (Vertex v : entry.getValue()) {
                    //FiguraMod.LOGGER.info(v.x + ", " + v.y + ", " + v.z);


                    dataVertices.put(String.valueOf(vertexId), new float[] {v.x, v.y, v.z});
                    vertexFaceBuffer.add(new Pair<>(v, vertexId));
                    // Check if buffer is ready to make a face.
                    if (vertexId % 4 == 0 && vertexId != 0) {
                        faceData.add(new MeshFaceData(vertexFaceBuffer, entry.getKey()));
                        // Clear the buffer
                        vertexFaceBuffer.clear();
                    }

                    vertexId++;
                }
            }

            MeshFaceData[] facesArray = new MeshFaceData[faceData.size()];
            faceData.toArray(facesArray);
            return new MeshData(dataVertices, facesArray);
        }
    }
}
