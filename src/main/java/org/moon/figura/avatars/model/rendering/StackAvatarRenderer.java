package org.moon.figura.avatars.model.rendering;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.config.Config;
import org.moon.figura.ducks.LivingEntityRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;

public class StackAvatarRenderer extends ImmediateAvatarRenderer {

    public StackAvatarRenderer(Avatar avatar) {
        super(avatar);
    }

    @Override
    protected void commonRender(double vertOffset) {
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
        viewToWorldMatrix = AvatarRenderer.worldToViewMatrix().inverted();

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
        float s = 1 / 16f;
        matrices.translate(0, vertOffset, 0);
        matrices.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrices.scale(s, s, s);

        PartCustomization customization = PartCustomization.of();

        customization.setPrimaryRenderType(RenderTypes.TRANSLUCENT);
        customization.setSecondaryRenderType(RenderTypes.EMISSIVE);

        customization.visible = true;
        customization.light = light;
        customization.alpha = alpha;
        customization.overlay = overlay;
        return customization;
    }

    @Override
    protected void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean parentPassedPredicate) {
        //part transforms
        if (entityRenderer != null) {
            if (part.parentType == ParentType.LeftElytra || part.parentType == ParentType.RightElytra)
                part.applyVanillaTransforms(((LivingEntityRendererAccessor<?>) entityRenderer).figura$getElytraModel());
            else
                part.applyVanillaTransforms(entityRenderer.getModel());
        }

        PartCustomization custom = part.customization;

        matrices.pushPose();
        custom.applyStack(matrices);
        part.applyExtraTransforms(matrices);

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = custom.visible;
        boolean thisPassedPredicate = currentFilterScheme.predicate.test(part, parentPassedPredicate);

        custom.visible = FiguraModelPart.getVisible(part) && thisPassedPredicate;
        customizationStack.push(custom);
        custom.visible = storedVisibility;

        PartCustomization peek = customizationStack.peek();

        //this feels wrong
        peek.positionMatrix.set(custom.positionMatrix);
        peek.normalMatrix.set(custom.normalMatrix);
        peek.uvMatrix.set(custom.uvMatrix);

        if (thisPassedPredicate) {
            //part to world matrices
            if (allowMatrixUpdate) {
                FiguraMat4 mat = partToWorldMatrices(part.customization);
                part.savedPartToWorldMat.set(mat);
                mat.free();
            }

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
                int light = peek.light;
                int overlay = peek.overlay;
                for (RenderTask task : part.renderTasks.values())
                    task.render(matrices, bufferSource, light, overlay);

                //render pivot parts
                if (ParentType.PIVOT_PARTS.contains(part.parentType))
                    applyPivotTransforms(part.parentType, matrices);
            }

            matrices.popPose();
        }

        //render
        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        //pop
        matrices.popPose();
        customizationStack.pop();
        part.resetVanillaTransforms();
    }
}
