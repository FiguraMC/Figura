package org.moon.figura.avatars.vanilla;

import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.math.vector.FiguraVec3;

public class VanillaPartOffsetManager {

    /**
     * Returns a NEW vector, so you can modify it and free it when you're done.
     * @param parentType
     * @return
     */
    public static FiguraVec3 getVanillaOffset(FiguraModelPart.ParentType parentType) {
        return switch (parentType) {
            case LeftArm -> FiguraVec3.of(5, 2, 0);
            case RightArm -> FiguraVec3.of(-5, 2, 0);
            case LeftLeg -> FiguraVec3.of(1.9, 12, 0);
            case RightLeg -> FiguraVec3.of(-1.9, 12, 0);

            case LeftElytra -> FiguraVec3.of(5, 0, 0);
            case RightElytra -> FiguraVec3.of(-5, 0, 0);

            default -> FiguraVec3.of();
        };
    }





}
