package org.moon.figura.avatars.model.rendering;

import net.minecraft.client.Minecraft;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.config.Config;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;

public class StackAvatarRenderer extends ImmediateAvatarRenderer {

    private static final PartCustomization pivotOffsetter = PartCustomization.of();

    public StackAvatarRenderer(Avatar avatar) {
        super(avatar);
    }

    @Override
    protected void commonRender(double vertOffset) {
        //clear pivot list

        //setup root customizations
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
        int config = Config.RENDER_DEBUG_PARTS_PIVOT.asInt();
        shouldRenderPivots = !Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() ? 0 : config;

        //Free customization after use
        customization.free();

        //world matrices
        if (allowMatrixUpdate)
            VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

        //Render all model parts
        int prev = avatar.trust.get(TrustContainer.Trust.COMPLEXITY) - avatar.complexity;
        int[] remainingComplexity = new int[] {prev};
        Boolean initialValue = currentFilterScheme.initialValue(root);
        if (initialValue != null)
            renderPart(root, remainingComplexity, initialValue);

        avatar.complexity += prev - Math.max(remainingComplexity[0], 0);

        customizationStack.pop();
        checkEmpty();
    }

    @Override
    protected void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
        PartCustomization custom = part.customization;

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = custom.visible;
        Boolean thisPassedPredicate = currentFilterScheme.test(part.parentType, prevPredicate);
        if (thisPassedPredicate == null)
            return;

        //calculate part transforms

        //calculate vanilla parent
        part.applyVanillaTransforms(entityRenderer);
        part.applyExtraTransforms(customizationStack.peek().positionMatrix);

        //push customization stack
        //that's right, check only for previous predicate
        boolean reset = !allowHiddenTransforms && !prevPredicate;
        if (reset) {
            custom.positionMatrix.reset();
            custom.normalMatrix.reset();
            custom.needsMatrixRecalculation = false;
        }

        custom.visible = part.getVisible() && thisPassedPredicate;
        custom.recalculate();
        customizationStack.push(custom);
        custom.visible = storedVisibility;

        if (reset) custom.needsMatrixRecalculation = true;

        //render this
        part.pushVerticesImmediate(this, remainingComplexity);

        //render extras
        if (thisPassedPredicate) {
            //part to world matrices
            if (allowMatrixUpdate) {
                FiguraMat4 mat = partToWorldMatrices(custom);
                part.savedPartToWorldMat.set(mat);
                mat.free();
            }

//            calculateWorldMatrices(part); //FOR DEBUG TESTING!

            PartCustomization peek = customizationStack.peek();

            //fix pivots
            FiguraVec3 pivot = custom.getPivot();
            FiguraVec3 offsetPivot = custom.getOffsetPivot();
            pivotOffsetter.setPos(pivot.add(offsetPivot));
            pivotOffsetter.recalculate();
            customizationStack.push(pivotOffsetter);
            pivot.free();
            offsetPivot.free();

            //render pivot indicators
            if (shouldRenderPivots > 1 || shouldRenderPivots == 1 && peek.visible)
                renderPivot(part);

            if (peek.visible) {
                //render tasks
                if (allowRenderTasks) {
                    int light = peek.light;
                    int overlay = peek.overlay;
                    allowSkullRendering = false;
                    for (RenderTask task : part.renderTasks.values()) {
                        int neededComplexity = task.getComplexity();
                        if (neededComplexity > remainingComplexity[0])
                            continue;
                        if (task.render(customizationStack, bufferSource, light, overlay))
                            remainingComplexity[0] -= neededComplexity;
                    }
                    allowSkullRendering = true;
                }

                //render pivot parts
                if (part.parentType.isPivot && allowPivotParts)
                    savePivotTransform(part.parentType);
            }

            customizationStack.pop();
        }

        //render children
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        //reset the parent
        part.resetVanillaTransforms();

        //pop
        customizationStack.pop();
    }
}
