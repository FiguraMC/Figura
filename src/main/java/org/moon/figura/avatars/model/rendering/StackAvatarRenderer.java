package org.moon.figura.avatars.model.rendering;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.config.Config;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;

public class StackAvatarRenderer extends ImmediateAvatarRenderer {

    public StackAvatarRenderer(Avatar avatar) {
        super(avatar);
    }

    @Override
    protected void commonRender(double vertOffset) {
        //clear pivot list
//        pivotCustomizations.clear();

        //setup root customizations
        matrices.pushPose();
        PartCustomization customization = setupRootCustomization(vertOffset);

        //Push transform
        customizationStack.push(customization);

        //Iterate and setup each buffer
        for (FiguraImmediateBuffer buffer : buffers) {
            //Reset buffers
            buffer.clearBuffers();
            //Upload texture if necessary
            buffer.uploadTexIfNeeded();
        }

        //Set shouldRenderPivots
        int config = (int) Config.RENDER_DEBUG_PARTS_PIVOT.value;
        shouldRenderPivots = !Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() ? 0 : config;

        //Free customization after use
        customization.free();

        //world matrices
        if (allowMatrixUpdate)
            VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

        //Render all model parts
        int prev = avatar.remainingComplexity;
        int[] remainingComplexity = new int[] {prev};
        renderPart(root, remainingComplexity, currentFilterScheme.initialValue);

        avatar.complexity += prev - remainingComplexity[0];
        avatar.remainingComplexity = remainingComplexity[0];

        customizationStack.pop();
        matrices.popPose();
        checkEmpty();
    }

    @Override
    protected PartCustomization setupRootCustomization(double vertOffset) {
        PartCustomization customization = super.setupRootCustomization(vertOffset);
        float s = 1 / 16f;
        matrices.translate(0, vertOffset, 0);
        matrices.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrices.scale(s, s, s);
        return customization;
    }

    @Override
    protected void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
        PartCustomization custom = part.customization;

        //calculate part transforms
        //we do not want to apply it straight away, so we check for the parent only
        matrices.pushPose();

        if (prevPredicate) {
            //calculate vanilla parent
            part.applyVanillaTransforms(entityRenderer);
            part.applyExtraTransforms(customizationStack.peek().positionMatrix);
        }

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = custom.visible;
        boolean thisPassedPredicate = currentFilterScheme.test(part.parentType, prevPredicate);

        //push customization stack
        custom.visible = part.getVisible() && thisPassedPredicate;
        custom.recalculate();
        custom.applyToStack(matrices);
        customizationStack.push(custom);
        custom.visible = storedVisibility;

        PartCustomization peek = customizationStack.peek();

        //render extras
        if (thisPassedPredicate) {
            //part to world matrices
            if (allowMatrixUpdate) {
                FiguraMat4 mat = partToWorldMatrices(custom);
                part.savedPartToWorldMat.set(mat);
                mat.free();
            }

//            calculateWorldMatrices(part); //FOR DEBUG TESTING!

            //fix pivots
            matrices.pushPose();

            FiguraVec3 pivot = custom.getPivot();
            FiguraVec3 offsetPivot = custom.getOffsetPivot();
            matrices.translate(
                    pivot.x + offsetPivot.x,
                    pivot.y + offsetPivot.y,
                    pivot.z + offsetPivot.z
            );
            pivot.free();
            offsetPivot.free();

            //render pivot indicators
            if (shouldRenderPivots > 1 || shouldRenderPivots == 1 && peek.visible)
                renderPivot(part, matrices);

            if (peek.visible) {
                //render tasks
                if (allowRenderTasks) {
                    int light = peek.light;
                    int overlay = peek.overlay;
                    for (RenderTask task : part.renderTasks.values())
                        task.render(matrices, bufferSource, light, overlay);
                }

                //render pivot parts
                if (ParentType.PIVOT_PARTS.contains(part.parentType))
                    applyPivotTransforms(part.parentType, matrices);
            }

            matrices.popPose();
        }

        //render this
        part.pushVerticesImmediate(this, remainingComplexity);

        //render children
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        //reset the parent
        part.resetVanillaTransforms();

        //pop
        customizationStack.pop();
        matrices.popPose();
    }
}
