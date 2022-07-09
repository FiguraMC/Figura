package org.moon.figura.animation;

import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.math.vector.FiguraVec3;

import java.util.function.BiConsumer;

public enum TransformType {
    POSITION(FiguraModelPart::animPosition),
    ROTATION(FiguraModelPart::animRotation),
    SCALE(FiguraModelPart::animScale);

    private final BiConsumer<FiguraModelPart, FiguraVec3> partConsumer;

    TransformType(BiConsumer<FiguraModelPart, FiguraVec3> partConsumer) {
        this.partConsumer = partConsumer;
    }

    public void apply(FiguraModelPart part, FiguraVec3 vec) {
        this.partConsumer.accept(part, vec);
    }
}
