package org.moon.figura.animation;

import org.moon.figura.model.FiguraModelPart;
import org.moon.figura.math.vector.FiguraVec3;

public enum TransformType {
    POSITION(FiguraModelPart::animPosition),
    ROTATION(FiguraModelPart::animRotation),
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
