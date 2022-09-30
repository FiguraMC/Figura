package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.FiguraModelPartReader;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTexture;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.config.Config;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    protected final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    protected final PartCustomization.Stack customizationStack = new PartCustomization.Stack();

    public static final FiguraMat4 VIEW_TO_WORLD_MATRIX = FiguraMat4.of();
    private static final PartCustomization pivotOffsetter = PartCustomization.of();

    public ImmediateAvatarRenderer(Avatar avatar) {
        super(avatar);

        //Vertex data, read model parts
        List<FiguraImmediateBuffer.Builder> builders = new ArrayList<>();
        root = FiguraModelPartReader.read(avatar, avatar.nbt.getCompound("models"), builders, textureSets);

        for (int i = 0; i < textureSets.size() && i < builders.size(); i++)
            buffers.add(builders.get(i).build(textureSets.get(i), customizationStack));
    }

    @Override
    protected void clean() {
        super.clean();
        customizationStack.fullClear();
        for (FiguraImmediateBuffer buffer : buffers)
            buffer.clean();
    }

    public void checkEmpty() {
        if (!customizationStack.isEmpty())
            throw new IllegalStateException("Customization stack not empty!");
    }

    @Override
    public int render() {
        return commonRender(1.5d);
    }

    @Override
    public int renderSpecialParts() {
        return commonRender(0);
    }

    protected int commonRender(double vertOffset) {
        //flag rendering state
        this.isRendering = true;

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

        //custom textures
        for (FiguraTexture texture : customTextures.values())
            texture.registerAndUpload();

        //Set shouldRenderPivots
        int config = Config.RENDER_DEBUG_PARTS_PIVOT.asInt();
        shouldRenderPivots = !Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() ? 0 : config;

        //Free customization after use
        customization.free();

        //world matrices
        if (allowMatrixUpdate)
            VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

        //Render all model parts
        int prev = avatar.complexity.remaining;
        int[] remainingComplexity = new int[] {prev};
        Boolean initialValue = currentFilterScheme.initialValue(root);
        if (initialValue != null)
            renderPart(root, remainingComplexity, initialValue);

        customizationStack.pop();
        checkEmpty();

        this.isRendering = false;
        if (this.dirty)
            clean();

        return prev - Math.max(remainingComplexity[0], 0);
    }

    protected PartCustomization setupRootCustomization(double vertOffset) {
        PartCustomization customization = PartCustomization.of();

        customization.setPrimaryRenderType(RenderTypes.TRANSLUCENT);
        customization.setSecondaryRenderType(RenderTypes.EMISSIVE);

        double s = 1.0 / 16;
        customization.positionMatrix.scale(s, s, s);
        customization.positionMatrix.rotateZ(180);
        customization.positionMatrix.translate(0, vertOffset, 0);
        customization.normalMatrix.rotateZ(180);

        FiguraMat4 posMat = FiguraMat4.fromMatrix4f(matrices.last().pose());
        FiguraMat3 normalMat = FiguraMat3.fromMatrix3f(matrices.last().normal());

        customization.positionMatrix.multiply(posMat);
        customization.normalMatrix.multiply(normalMat);

        posMat.free();
        normalMat.free();

        customization.visible = true;
        customization.light = light;
        customization.alpha = alpha;
        customization.overlay = overlay;

        customization.primaryTexture = Pair.of(FiguraTextureSet.OverrideType.PRIMARY, null);
        customization.secondaryTexture = Pair.of(FiguraTextureSet.OverrideType.SECONDARY, null);

        return customization;
    }

    protected boolean renderPart(FiguraModelPart part, int[] remainingComplexity, boolean prevPredicate) {
        PartCustomization custom = part.customization;

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = custom.visible;
        Boolean thisPassedPredicate = currentFilterScheme.test(part.parentType, prevPredicate);
        if (thisPassedPredicate == null) {
            part.advanceVerticesImmediate(this); //stinky
            return true;
        }

        //calculate part transforms

        //calculate vanilla parent
        part.applyVanillaTransforms(vanillaModelData);
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
        if (!part.pushVerticesImmediate(this, remainingComplexity)) {
            customizationStack.pop();
            return false;
        }

        //render extras
        if (thisPassedPredicate) {
            //part to world matrices
            if (allowMatrixUpdate) {
                FiguraMat4 mat = partToWorldMatrices(custom);
                part.savedPartToWorldMat.set(mat);
                mat.free();
            }

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
            if (!renderPart(child, remainingComplexity, thisPassedPredicate)) {
                customizationStack.pop();
                return false;
            }

        //reset the parent
        part.resetVanillaTransforms();

        //pop
        customizationStack.pop();

        return true;
    }

    protected void renderPivot(FiguraModelPart part) {
        boolean group = part.customization.partType == PartCustomization.PartType.GROUP;
        FiguraVec3 color = group ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = group ? 1 / 16d : 1 / 32d;
        boxSize /= Math.max(Math.cbrt(part.savedPartToWorldMat.det()), 0.02);

        PoseStack stack = customizationStack.peek().copyIntoGlobalPoseStack();

        LevelRenderer.renderLineBox(stack, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    protected void savePivotTransform(ParentType parentType) {
        FiguraMat4 currentPosMat = customizationStack.peek().getPositionMatrix();
        FiguraMat3 currentNormalMat = customizationStack.peek().getNormalMatrix();
        ConcurrentLinkedQueue<Pair<FiguraMat4, FiguraMat3>> queue = pivotCustomizations.computeIfAbsent(parentType, p -> new ConcurrentLinkedQueue<>());
        queue.add(new Pair<>(currentPosMat, currentNormalMat)); //These are COPIES, so ok to add
    }

    protected FiguraMat4 partToWorldMatrices(PartCustomization cust) {
        FiguraMat4 customizePeek = customizationStack.peek().positionMatrix.copy();
        customizePeek.multiply(VIEW_TO_WORLD_MATRIX);
        FiguraVec3 piv = cust.getPivot();

        FiguraMat4 translation = FiguraMat4.of();
        translation.translate(piv);
        customizePeek.rightMultiply(translation);

        piv.free();
        translation.free();

        return customizePeek;
    }

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(this, faceCount, remainingComplexity);
    }

    public void advanceFaces(int texIndex, int faceCount) {
        buffers.get(texIndex).advanceBuffers(faceCount);
    }
}
