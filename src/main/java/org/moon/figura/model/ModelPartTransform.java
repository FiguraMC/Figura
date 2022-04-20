package org.moon.figura.model;

import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;

public class ModelPartTransform {

    public final FiguraMat4 positionMatrix = FiguraMat4.of();
    public final FiguraMat3 normalMatrix = FiguraMat3.of();

    public boolean needsMatrixRecalculation = true;

    public final FiguraVec3 position = FiguraVec3.of();
    public final FiguraVec3 rotation = FiguraVec3.of();
    public final FiguraVec3 scale = FiguraVec3.of(1, 1, 1);
    public final FiguraVec3 pivot = FiguraVec3.of();

    /**
     * Recalculates the matrix if necessary.
     */
    public void recalculate() {
        if (needsMatrixRecalculation) {
            positionMatrix.reset();
            positionMatrix.translate(-pivot.x, -pivot.y, -pivot.z);
            positionMatrix.scale(scale.x, scale.y, scale.z);
            positionMatrix.translate(position.x, position.y, position.z);
            positionMatrix.rotateZYX(rotation.x, rotation.y, rotation.z);
            positionMatrix.translate(pivot.x, pivot.y, pivot.z);

            normalMatrix.reset();
            double c = Math.cbrt(scale.x * scale.y * scale.z);
            normalMatrix.scale(
                    c == 0 && scale.x == 0 ? 1 : c / scale.x,
                    c == 0 && scale.y == 0 ? 1 : c / scale.y,
                    c == 0 && scale.z == 0 ? 1 : c / scale.z
            );
            normalMatrix.rotateZYX(rotation.x, rotation.y, rotation.z);

            needsMatrixRecalculation = false;
        }
    }

}
