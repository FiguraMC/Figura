package org.figuramc.figura.parsers.Buwwet;

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

/// Parses Figura models into blockbench models by performing all of the calculations already done previously but on reverse.
public class FiguraModelParser {

    public static void parseAvatar(CompoundTag nbt) {
        // Get textures (required for some vector parsers).
        CompoundTag texturesNbt = nbt.getCompound("textures");

        //ListTag texturesList = texturesNbt.getList("data", Tag.TAG_COMPOUND);



        BlockBenchPart rootFiguraModel = BlockBenchPart.parseNBTchildren(nbt.getCompound("models"));

    }
    public static class CubeData {
        static final List<String> FACES = List.of("north", "south", "west", "east", "up", "down");

        public String name;
        /// 4-sized length
        public float[] uv;
        public int texture;

        public CubeData(String name, float[] uv, int texture) {
            this.name = name;
            this.uv = uv;
            this.texture = texture;
        }

        public static CubeData[] generateFromFiguraFaces(Tag faces) {
            CompoundTag facesNBT = (CompoundTag) faces;
            //FiguraMod.LOGGER.info(faces.getAsString());

            // POSSIBLE BLOCKBENCH ERROR: Some elements do not have any faces but have the "cube_data" field.
            if (((CompoundTag) faces).size() == 0) {
                return new CubeData[0];
            }

            ArrayList<CubeData> finalFaces = new ArrayList<>();
            // Figura completely butchers the names of the faces, we need to find them again and put the correct one.
            for (String faceName : CubeData.FACES) {
                CompoundTag faceNbt = (CompoundTag) facesNBT.get(String.valueOf(faceName.charAt(0)));
                // Get the uv and texture index
                float[] uv = BlockBenchPart.fillVectorIfNone(faceNbt.get("uv"), 4);
                int texture = facesNBT.getInt("tex");

                finalFaces.add(new CubeData(faceName, uv, texture));
                //FiguraMod.LOGGER.info(faceName);
            }

            CubeData[] array = new CubeData[finalFaces.size()];
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
                FiguraMod.LOGGER.info(String.valueOf(entry.getKey()));

                for (Vertex v : entry.getValue()) {
                    FiguraMod.LOGGER.info(v.x + ", " + v.y + ", " + v.z);


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
