package org.moon.figura.model;

import net.minecraft.nbt.CompoundTag;
import org.moon.figura.model.rendering.FiguraBuffer;

import java.util.List;

public class FiguraModelPart {

    private ModelPartTransform transform;
    private int index;
    private List<FiguraModelPart> children;


    public static FiguraModelPart create(CompoundTag partCompound, FiguraBuffer.Builder vertexBuffer) {
        return create(partCompound, vertexBuffer, new int[] {0});
    }

    private static FiguraModelPart create(CompoundTag partCompound, FiguraBuffer.Builder vertexBuffer, int[] index) {
        FiguraModelPart result = new FiguraModelPart();
        result.transform = new ModelPartTransform();
        result.index = index[0];
        //TODO: stuff
        return result;
    }



}
