package org.figuramc.figura.animation;

import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.FiguraModelPart;

public enum TransformType {
    POSITION(FiguraModelPart::animPosition),
    ROTATION(FiguraModelPart::animRotation),
    GLOBAL_ROT(FiguraModelPart::globalAnimRot),
    SCALE(FiguraModelPart::animScale);

    private final ITransform function;

    TransformType(ITransform function) {
        this.function = function;
    }

    public void apply(FiguraModelPart part, FiguraVec3 vec, boolean merge) {
        this.function.apply(part, vec, merge);
    }

    private interface ITransform {
        void apply(FiguraModelPart part, FiguraVec3 vec, boolean merge);
    }
}
