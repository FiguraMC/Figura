package org.figuramc.figura.parsers.Buwwet;

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

    public class MeshData {
        public HashMap<String, float[]> verticies;
        public MeshFaceData[] faces;

        public class MeshFaceData {
            // uvs are sized 2
            public HashMap<String, float[]> uv;
            public String[] verticies;

            public int texture;

        }

        public static void generateFromElement(CompoundTag element) {
            List<Integer> facesByTexture = new ArrayList<>();
            // Temp because we still aren't catching textures
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);
            facesByTexture.add(0);

            Map<Integer, List<Vertex>> vertices = new HashMap<>();

            FiguraModelPartReader.readMesh(facesByTexture, element, vertices);
            for (Map.Entry<Integer, List<Vertex>> entry : vertices.entrySet()) {
                FiguraMod.LOGGER.info(String.valueOf(entry.getKey()));
                for (Vertex v : entry.getValue()) {
                    FiguraMod.LOGGER.info(v.x + ", " + v.y + ", " + v.z);
                }
            }
        }
    }
}
