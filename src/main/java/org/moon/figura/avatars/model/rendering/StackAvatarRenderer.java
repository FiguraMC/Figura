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
        if (entityRenderer != null) {
            if (part.parentType == ParentType.LeftElytra || part.parentType == ParentType.RightElytra)
                part.applyVanillaTransforms(((LivingEntityRendererAccessor<?>) entityRenderer).figura$getElytraModel());
            else
                part.applyVanillaTransforms(entityRenderer.getModel());
        }
        part.applyExtraTransforms();

        matrices.pushPose();
        part.customization.applyStack(matrices);

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = part.customization.visible;
        boolean thisPassedPredicate = currentFilterScheme.predicate.test(part, parentPassedPredicate);

        part.customization.visible = FiguraModelPart.getVisible(part) && thisPassedPredicate;
        customizationStack.push(part.customization);
        part.customization.visible = storedVisibility;

        PartCustomization peek = customizationStack.peek();

        //this feels wrong
        peek.positionMatrix.set(part.customization.positionMatrix);
        peek.normalMatrix.set(part.customization.normalMatrix);
        peek.uvMatrix.set(part.customization.uvMatrix);

        if (thisPassedPredicate) {
            //pivots
            if (shouldRenderPivots > 1 || shouldRenderPivots == 1 && peek.visible)
                renderPivot(part, matrices);

            if (peek.visible) {
                //tasks
                int light = peek.light;
                int overlay = peek.overlay;
                for (RenderTask task : part.renderTasks.values())
                    task.render(matrices, bufferSource, light, overlay);

                //pivot parts
                if (ParentType.PIVOT_PARTS.contains(part.parentType))
                    applyPivotTransforms(part.parentType, matrices);
            }
        }

        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        matrices.popPose();
        customizationStack.pop();
        part.resetVanillaTransforms();
    }
}
