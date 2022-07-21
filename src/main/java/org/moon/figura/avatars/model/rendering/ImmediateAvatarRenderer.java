package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.*;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.config.Config;
import org.moon.figura.ducks.LivingEntityRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    protected final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    protected final PartCustomization.Stack customizationStack = new PartCustomization.Stack();

    private static final PoseStack VIEW_MATRICES = new PoseStack();
    private static FiguraMat4 viewToWorldMatrix = FiguraMat4.of();

    public ImmediateAvatarRenderer(Avatar avatar) {
        super(avatar);

        //Textures
        List<FiguraTextureSet> textureSets = new ArrayList<>();
        ListTag texturesList = avatar.nbt.getList("textures", Tag.TAG_COMPOUND);
        for (int i = 0; i < texturesList.size(); i++) {
            CompoundTag tag = texturesList.getCompound(i);

            String name = tag.getString("name");

            byte[] mainData = tag.getByteArray("default");
            mainData = mainData.length == 0 ? null : mainData;

            byte[] emissiveData = tag.getByteArray("emissive");
            emissiveData = emissiveData.length == 0 ? null : emissiveData;

            textureSets.add(new FiguraTextureSet(name, mainData, emissiveData));
        }

        //Vertex data, read model parts
        List<FiguraImmediateBuffer.Builder> builders = new ArrayList<>();
        root = FiguraModelPartReader.read(avatar, avatar.nbt.getCompound("models"), builders, textureSets);

        for (int i = 0; i < textureSets.size() && i < builders.size(); i++)
            buffers.add(builders.get(i).build(textureSets.get(i), customizationStack));

        avatar.hasTexture = !texturesList.isEmpty();
    }

    @Override
    public void clean() {
        customizationStack.fullClear();
        for (FiguraImmediateBuffer buffer : buffers)
            buffer.clean();
    }

    public void checkEmpty() {
        if (!customizationStack.isEmpty())
            throw new IllegalStateException("Customization stack not empty!");
    }

    @Override
    public void render() {
        commonRender(1.5d);
    }

    @Override
    public void renderSpecialParts() {
        commonRender(0);
    }

    protected void commonRender(double vertOffset) {
        //Push position and normal matrices
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
        if (allowMatrixUpdate)
            viewToWorldMatrix = AvatarRenderer.worldToViewMatrix().inverted();

        int prev = avatar.remainingComplexity;
        int[] remainingComplexity = new int[] {prev};
        renderPart(root, remainingComplexity, currentFilterScheme.initialValue);

        avatar.complexity += prev - remainingComplexity[0];
        avatar.remainingComplexity = remainingComplexity[0];

        customizationStack.pop();
        checkEmpty();
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
        return customization;
    }

    protected void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean parentPassedPredicate) {
        if (entityRenderer != null) {
            if (part.parentType == ParentType.LeftElytra || part.parentType == ParentType.RightElytra)
                part.applyVanillaTransforms(((LivingEntityRendererAccessor<?>) entityRenderer).figura$getElytraModel());
            else
                part.applyVanillaTransforms(entityRenderer.getModel());
        }
        part.applyExtraTransforms();

        part.customization.recalculate();

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = part.customization.visible;
        boolean thisPassedPredicate = currentFilterScheme.predicate.test(part, parentPassedPredicate);

        part.customization.visible = FiguraModelPart.getVisible(part) && thisPassedPredicate;
        customizationStack.push(part.customization);
        part.customization.visible = storedVisibility;

        PartCustomization peek = customizationStack.peek();

        //Right now, part.customization.positionMatrix contains a transformation from part space to view space.
        if (thisPassedPredicate && allowMatrixUpdate) {
            FiguraMat4 customizePeek = peek.positionMatrix.copy();
            customizePeek.multiply(viewToWorldMatrix);
            FiguraVec3 piv = part.customization.getPivot();
            FiguraVec3 pos = part.customization.getPos();
            piv.subtract(pos);

            FiguraMat4 translation = FiguraMat4.createTranslationMatrix(piv);
            customizePeek.rightMultiply(translation);
            part.savedPartToWorldMat.set(customizePeek);

            customizePeek.free();
            piv.free();
            pos.free();
            translation.free();
        }

        if (thisPassedPredicate) {
            calculateWorldMatrices(part);

            //pivots
            if (shouldRenderPivots > 1 || shouldRenderPivots == 1 && peek.visible)
                renderPivot(part, VIEW_MATRICES);

            if (peek.visible) {
                //tasks
                int light = peek.light;
                int overlay = peek.overlay;
                for (RenderTask task : part.renderTasks.values())
                    task.render(VIEW_MATRICES, bufferSource, light, overlay);

                //pivot parts
                if (ParentType.PIVOT_PARTS.contains(part.parentType))
                    applyPivotTransforms(part.parentType, VIEW_MATRICES);
            }
        }

        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        customizationStack.pop();

        part.resetVanillaTransforms();
    }

    protected void renderPivot(FiguraModelPart part, PoseStack stack) {
        //Index == -1 means it's a group
        FiguraVec3 color = part.index == -1 ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = part.index == -1 ? 1 / 16d : 1 / 32d;
        boxSize /= Math.cbrt(part.savedPartToWorldMat.det());

        LevelRenderer.renderLineBox(stack, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    private void calculateWorldMatrices(FiguraModelPart part) {
        VIEW_MATRICES.setIdentity();

        FiguraMat4 posMat = part.savedPartToWorldMat.copy();
        FiguraMat4 worldToView = AvatarRenderer.worldToViewMatrix();
        VIEW_MATRICES.mulPoseMatrix(worldToView.toMatrix4f());
        VIEW_MATRICES.mulPoseMatrix(posMat.toMatrix4f());

        worldToView.free();
        posMat.free();
    }

    protected void applyPivotTransforms(ParentType parentType, PoseStack stack) {
        Transformable transformable = new Transformable();
        transformable.pose = stack.last();
        this.pivotCustomizations.put(parentType, transformable);
    }

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(bufferSource, faceCount, remainingComplexity);
    }
}
