package org.moon.figura.avatars.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.caching.CacheStack;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

public class PartCustomization implements CachedType {

    //-- Matrix thingies --//
    /**
     * Boolean exists because blockbench sucks and uses a different rotation
     * formula for meshes than other types of items. Meshes rotate in XYZ order,
     * while literally everything else is in ZYX order.
     */
    public PartType partType = PartType.GROUP;

    public FiguraMat4 positionMatrix = FiguraMat4.of();
    public FiguraMat3 uvMatrix = FiguraMat3.of();
    public FiguraMat3 normalMatrix = FiguraMat3.of();

    public boolean needsMatrixRecalculation = true;
    public Boolean visible = null;

    private FiguraVec3 position = FiguraVec3.of();
    private FiguraVec3 rotation = FiguraVec3.of();
    private FiguraVec3 scale = FiguraVec3.of(1, 1, 1);
    private FiguraVec3 pivot = FiguraVec3.of();

    //The "offset" values are for vanilla part scaling. The offset pivot and rot can be get and set from script.
    private FiguraVec3 offsetPivot = FiguraVec3.of();
    private FiguraVec3 offsetPos = FiguraVec3.of();
    private FiguraVec3 offsetRot = FiguraVec3.of();

    //These values are set by animation players. They can be queried, though not set, by script.
    private FiguraVec3 animPos = FiguraVec3.of();
    private FiguraVec3 animRot = FiguraVec3.of();
    private FiguraVec3 animScale = FiguraVec3.of(1, 1, 1);

    public FiguraVec3 color = FiguraVec3.of(1, 1, 1);
    public Float alpha = null;
    public Integer light = null;
    public Integer overlay = null;

    /**
     * Recalculates the matrix if necessary.
     */
    public void recalculate() {
        if (needsMatrixRecalculation) {
            positionMatrix.reset();

            //Position the pivot point at 0, 0, 0, and translate the part
            positionMatrix.translate(
                    position.x + offsetPos.x + animPos.x - pivot.x - offsetPivot.x,
                    position.y + offsetPos.y + animPos.y - pivot.y - offsetPivot.y,
                    position.z + offsetPos.z + animPos.z - pivot.z - offsetPivot.z
            );

            //Scale the model part around the pivot
            positionMatrix.scale(
                    scale.x*animScale.x,
                    scale.y*animScale.y,
                    scale.z*animScale.z
            );

            //Rotate the model part around the pivot
            if (partType == PartType.MESH) {
                positionMatrix.rotateZ(rotation.z + offsetRot.z + animRot.z);
                positionMatrix.rotateY(rotation.y + offsetRot.y + animRot.y);
                positionMatrix.rotateX(rotation.x + offsetRot.x + animRot.x);
            } else {
                positionMatrix.rotateZYX(
                        rotation.x + offsetRot.x + animRot.x,
                        rotation.y + offsetRot.y + animRot.y,
                        rotation.z + offsetRot.z + animRot.z
                );
            }

            //Undo the effects of the pivot translation
            positionMatrix.translate(
                    pivot.x + offsetPivot.x,
                    pivot.y + offsetPivot.y,
                    pivot.z + offsetPivot.z
            );

            //Set up the normal matrix as well
            normalMatrix.reset();
            double x = scale.x * animScale.x;
            double y = scale.y * animScale.y;
            double z = scale.z * animScale.z;
            double c = Math.cbrt(x * y * z);
            normalMatrix.scale(
                    c == 0 && x == 0 ? 1 : c / x,
                    c == 0 && y == 0 ? 1 : c / y,
                    c == 0 && z == 0 ? 1 : c / z
            );

            //Perform rotation of normals
            if (partType == PartType.MESH) {
                normalMatrix.rotateZ(rotation.z + offsetRot.z + animRot.z);
                normalMatrix.rotateY(rotation.y + offsetRot.y + animRot.y);
                normalMatrix.rotateX(rotation.x + offsetRot.x + animRot.x);
            } else {
                normalMatrix.rotateZYX(
                        rotation.x + offsetRot.x + animRot.x,
                        rotation.y + offsetRot.y + animRot.y,
                        rotation.z + offsetRot.z + animRot.z
                );
            }

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
        return position.copy();
    }

    public void setRot(FiguraVec3 rot) {
        setRot(rot.x, rot.y, rot.z);
    }
    public void setRot(double x, double y, double z) {
        rotation.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getRot() {
        return rotation.copy();
    }

    public void setScale(FiguraVec3 scale) {
        setScale(scale.x, scale.y, scale.z);
    }
    public void setScale(double x, double y, double z) {
        scale.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getScale() {
        return scale.copy();
    }

    public void setPivot(FiguraVec3 pivot) {
        setPivot(pivot.x, pivot.y, pivot.z);
    }
    public void setPivot(double x, double y, double z) {
        pivot.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getPivot() {
        return pivot.copy();
    }

    public void offsetPivot(FiguraVec3 pivot) {
        offsetPivot(pivot.x, pivot.y, pivot.z);
    }
    public void offsetPivot(double x, double y, double z) {
        offsetPivot.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getOffsetPivot() {
        return offsetPivot.copy();
    }

    public void offsetPos(FiguraVec3 pos) {
        offsetPos(pos.x, pos.y, pos.z);
    }
    public void offsetPos(double x, double y, double z) {
        offsetPos.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getOffsetPos() {
        return offsetPos.copy();
    }

    public void offsetRot(FiguraVec3 rot) {
        offsetRot(rot.x, rot.y, rot.z);
    }
    public void offsetRot(double x, double y, double z) {
        offsetRot.set(x, y, z);
        needsMatrixRecalculation = true;
    }
    public FiguraVec3 getOffsetRot() {
        return offsetRot.copy();
    }

    public FiguraVec3 getAnimPos() {
        return animPos.copy();
    }
    public FiguraVec3 getAnimRot() {
        return animRot.copy();
    }
    public FiguraVec3 getAnimScale() {
        return animScale.copy();
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

    private FiguraTextureSet.RenderTypes primaryRenderType;
    private FiguraTextureSet.RenderTypes secondaryRenderType;
    public ResourceLocation primaryTexture = null;
    public ResourceLocation secondaryTexture = null;

    public void setPrimaryRenderType(FiguraTextureSet.RenderTypes type) {
        primaryRenderType = type;
    }
    public FiguraTextureSet.RenderTypes getPrimaryRenderType() {
        return primaryRenderType;
    }
    public void setSecondaryRenderType(FiguraTextureSet.RenderTypes type) {
        secondaryRenderType = type;
    }
    public FiguraTextureSet.RenderTypes getSecondaryRenderType() {
        return secondaryRenderType;
    }


    //-- Caching thingies --//

    private static final CacheUtils.Cache<PartCustomization> CACHE = CacheUtils.getCache(PartCustomization::new);
    private PartCustomization() {}
    public void reset() {
        positionMatrix = FiguraMat4.of();
        uvMatrix = FiguraMat3.of();
        normalMatrix = FiguraMat3.of();
        partType = PartType.GROUP;
        position = FiguraVec3.of();
        rotation = FiguraVec3.of();
        scale = FiguraVec3.of(1, 1, 1);
        pivot = FiguraVec3.of();
        offsetPivot = FiguraVec3.of();
        offsetPos = FiguraVec3.of();
        offsetRot = FiguraVec3.of();
        color = FiguraVec3.of(1, 1, 1);
        alpha = null;
        light = null;
        needsMatrixRecalculation = false;
        visible = null;
        primaryTexture = null;
        secondaryTexture = null;
    }
    public void free() {
        positionMatrix.free();
        uvMatrix.free();
        normalMatrix.free();
        position.free();
        rotation.free();
        scale.free();
        pivot.free();
        offsetPivot.free();
        offsetPos.free();
        offsetRot.free();
        color.free();
    }
    public static PartCustomization of() {
        return CACHE.getFresh();
    }
    public static class Stack extends CacheStack<PartCustomization, PartCustomization> {

        /**
         * A vanilla pose stack useful for certain UI elements.
         * Mirrors the matrices of the customizations on the stack.
         */
        public final PoseStack poseStack = new PoseStack();

        public Stack() {
            this(CACHE);
        }
        public Stack(CacheUtils.Cache<PartCustomization> cache) {
            super(cache);
        }

        @Override
        public void push(PartCustomization modifier) {
            super.push(modifier);
            poseStack.pushPose();
            poseStack.mulPoseMatrix(modifier.positionMatrix.toMatrix4f());
        }

        @Override
        public PartCustomization pop() {
            poseStack.popPose();
            return super.pop();
        }

        @Override
        protected void modify(PartCustomization valueToModify, PartCustomization modifierArg) {
            valueToModify.modify(modifierArg);
        }
        @Override
        protected void copy(PartCustomization from, PartCustomization to) {
            to.partType = from.partType;
            to.positionMatrix.set(from.positionMatrix);
            to.uvMatrix.set(from.uvMatrix);
            to.normalMatrix.set(from.normalMatrix);
            to.setPos(from.position);
            to.setRot(from.rotation);
            to.setScale(from.scale);
            to.setPivot(from.pivot);
            to.offsetPivot(from.offsetPivot);
            to.offsetPos(from.offsetPos);
            to.offsetRot(from.offsetRot);
            to.color.set(from.color);
            to.alpha = from.alpha;
            to.light = from.light;
            to.overlay = from.overlay;
            to.needsMatrixRecalculation = from.needsMatrixRecalculation;
            to.visible = from.visible;
            to.setPrimaryRenderType(from.primaryRenderType);
            to.setSecondaryRenderType(from.secondaryRenderType);
            to.primaryTexture = from.primaryTexture;
            to.secondaryTexture = from.secondaryTexture;
        }
    }

    //Modify this object using the information contained in the other object
    private void modify(PartCustomization other) {
        positionMatrix.rightMultiply(other.positionMatrix);
        uvMatrix.rightMultiply(other.uvMatrix);
        normalMatrix.rightMultiply(other.normalMatrix);

        if (other.primaryRenderType != null)
            setPrimaryRenderType(other.primaryRenderType);
        if (other.secondaryRenderType != null)
            setSecondaryRenderType(other.secondaryRenderType);

        if (other.visible != null)
            visible = other.visible;

        if (other.light != null)
            light = other.light;

        if (other.overlay != null)
            overlay = other.overlay;

        if (other.alpha != null) {
            if (alpha != null)
                alpha *= other.alpha;
            else
                alpha = other.alpha;
        }

        color.multiply(other.color);

        if (other.primaryTexture != null)
            primaryTexture = other.primaryTexture;
        if (other.secondaryTexture != null)
            secondaryTexture = other.secondaryTexture;
    }

    public enum PartType {
        MESH,
        CUBE,
        GROUP
    }
}
