package org.figuramc.figura.parsers.Buwwet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.luaj.vm2.ast.Str;

import java.util.ArrayList;
import java.util.List;

/// Parses Figura models into blockbench models by performing all of the calculations already done previously but on reverse.
public class FiguraModelParser {
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

    }
}
