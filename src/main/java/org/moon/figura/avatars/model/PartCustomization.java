package org.moon.figura.avatars.model;

import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.avatars.model.rendering.FiguraImmediateBuffer;
import org.moon.figura.utils.caching.CacheStack;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

public class PartCustomization implements CachedType {

    //-- Matrix thingies --//

    public FiguraMat4 positionMatrix = FiguraMat4.of();
    public FiguraMat3 normalMatrix = FiguraMat3.of();

    public boolean needsMatrixRecalculation = true;
    public boolean visible = true;

    private FiguraVec3 position = FiguraVec3.of();
    private FiguraVec3 rotation = FiguraVec3.of();
    private FiguraVec3 scale = FiguraVec3.of(1, 1, 1);
    private FiguraVec3 pivot = FiguraVec3.of();


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

    public void setPos(FiguraVec3 pos) {
        setPos(pos.x, pos.y, pos.z);
    }
    public void setPos(double x, double y, double z) {
        position.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getPos() {
        return FiguraVec3.of(position.x, position.y, position.z);
    }
    public void setRot(FiguraVec3 rot) {
        setRot(rot.x, rot.y, rot.z);
    }
    public void setRot(double x, double y, double z) {
        rotation.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getRot() {
        return FiguraVec3.of(rotation.x, rotation.y, rotation.z);
    }
    public void setScale(FiguraVec3 scale) {
        setScale(scale.x, scale.y, scale.z);
    }
    public void setScale(double x, double y, double z) {
        scale.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getScale() {
        return FiguraVec3.of(scale.x, scale.y, scale.z);
    }
    public void setPivot(FiguraVec3 pivot) {
        setPivot(pivot.x, pivot.y, pivot.z);
    }
    public void setPivot(double x, double y, double z) {
        pivot.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getPivot() {
        return FiguraVec3.of(pivot.x, pivot.y, pivot.z);
    }

    public void setMatrix(FiguraMat4 matrix) {
        positionMatrix.set(matrix);
        FiguraMat3 temp = matrix.deaugmented();
        temp.invert();
        temp.transpose();
        normalMatrix.set(temp);
        temp.free();
        needsMatrixRecalculation = false;
    }
    public FiguraMat4 getPositionMatrix() {
        FiguraMat4 result = FiguraMat4.of();
        result.set(positionMatrix);
        return result;
    }
    public FiguraMat3 getNormalMatrix() {
        FiguraMat3 result = FiguraMat3.of();
        result.set(normalMatrix);
        return result;
    }

    //-- Render type thingies --//

    private String primaryRenderType;
    private String secondaryRenderType;

    public void setPrimaryRenderType(String type) {
        primaryRenderType = type;
    }
    public String getPrimaryRenderType() {
        return primaryRenderType;
    }
    public void setSecondaryRenderType(String type) {
        secondaryRenderType = type;
    }
    public String getSecondaryRenderType() {
        return secondaryRenderType;
    }


    //-- Caching thingies --//

    private static final CacheUtils.Cache<PartCustomization> CACHE = CacheUtils.getCache(PartCustomization::new);
    private PartCustomization() {}
    public void reset() {
        positionMatrix = FiguraMat4.of();
        normalMatrix = FiguraMat3.of();
        position = FiguraVec3.of();
        rotation = FiguraVec3.of();
        scale = FiguraVec3.of(1, 1, 1);
        pivot = FiguraVec3.of();
        needsMatrixRecalculation = false;
        visible = true;
    }
    public void free() {
        positionMatrix.free();
        normalMatrix.free();
        position.free();
        rotation.free();
        scale.free();
        pivot.free();
    }
    public static PartCustomization of() {
        return CACHE.getFresh();
    }
    public static class Stack extends CacheStack<PartCustomization, PartCustomization> {
        public Stack() {
            this(CACHE);
        }
        public Stack(CacheUtils.Cache<PartCustomization> cache) {
            super(cache);
        }
        @Override
        protected void modify(PartCustomization valueToModify, PartCustomization modifierArg) {
            valueToModify.modify(modifierArg);
        }
        @Override
        protected void copy(PartCustomization from, PartCustomization to) {
            to.positionMatrix.set(from.positionMatrix);
            to.normalMatrix.set(from.normalMatrix);
            to.setPos(from.position);
            to.setRot(from.rotation);
            to.setScale(from.scale);
            to.setPivot(from.pivot);
            to.needsMatrixRecalculation = from.needsMatrixRecalculation;
            to.setPrimaryRenderType(from.primaryRenderType);
            to.setSecondaryRenderType(from.secondaryRenderType);
        }
    }

    //Modify this object using the information contained in the other object
    private void modify(PartCustomization other) {
        positionMatrix.rightMultiply(other.positionMatrix);
        normalMatrix.rightMultiply(other.normalMatrix);

        if (other.primaryRenderType != null)
            setPrimaryRenderType(other.primaryRenderType);
        if (other.secondaryRenderType != null)
            setSecondaryRenderType(other.secondaryRenderType);

        if (!other.visible)
            visible = false; //Default state is assumed to be visible
    }


    //-- Push function --//

    public void pushToBuffer(FiguraImmediateBuffer buffer) {
        recalculate();
        buffer.pushCustomization(this);
    }

}
