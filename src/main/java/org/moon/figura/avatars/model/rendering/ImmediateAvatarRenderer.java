package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.FiguraModelPartReader;
import org.moon.figura.avatars.model.ParentType;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.avatars.model.rendering.texture.RenderTypes;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.config.Config;
import org.moon.figura.ducks.PoseStackAccessor;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    protected final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    protected final PartCustomization.Stack customizationStack = new PartCustomization.Stack();

    protected static final PoseStack VIEW_MATRICES = new PoseStack();
    public static final FiguraMat4 VIEW_TO_WORLD_MATRIX = FiguraMat4.of();

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

    @Deprecated
    protected void commonRender(double vertOffset) {
        //clear pivot list
        pivotCustomizations.clear();

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
            VIEW_TO_WORLD_MATRIX.set(AvatarRenderer.worldToViewMatrix().invert());

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

    @Deprecated
    //Method is only kept for reference of how it used to work, this impl no longer works with new system.
    protected void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean parentPassedPredicate) {
        part.applyVanillaTransforms(entityRenderer);

        part.applyExtraTransforms(customizationStack.peek().positionMatrix);

        part.customization.recalculate();

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = part.customization.visible;
        boolean thisPassedPredicate = currentFilterScheme.test(part.parentType, parentPassedPredicate);

        part.customization.visible = part.getVisible() && thisPassedPredicate;
        customizationStack.push(part.customization);
        part.customization.visible = storedVisibility;

        PartCustomization peek = customizationStack.peek();

        //Right now, part.customization.positionMatrix contains a transformation from part space to view space.
        if (thisPassedPredicate && allowMatrixUpdate) {
            FiguraMat4 matrices = partToWorldMatrices(part.customization);
            part.savedPartToWorldMat.set(matrices);
            matrices.free();
        }

        if (thisPassedPredicate) {
            calculateWorldMatrices(part);

            //pivots
            if (shouldRenderPivots > 1 || shouldRenderPivots == 1 && peek.visible)
//                renderPivot(part, VIEW_MATRICES);
                renderPivot(part);

//            if (peek.visible) {
//                //tasks
//                int light = peek.light;
//                int overlay = peek.overlay;
//                for (RenderTask task : part.renderTasks.values())
//                    task.render(VIEW_MATRICES, bufferSource, light, overlay);
//
//                //pivot parts
//                if (part.parentType.isPivot)
//                    savePivotTransform(part.parentType, VIEW_MATRICES);
//            }
        }

        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        customizationStack.pop();

        part.resetVanillaTransforms();
    }

    protected void renderPivot(FiguraModelPart part) {
        boolean group = part.customization.partType == PartCustomization.PartType.GROUP;
        FiguraVec3 color = group ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = group ? 1 / 16d : 1 / 32d;
        boxSize /= Math.cbrt(part.savedPartToWorldMat.det());

        PoseStack stack = customizationStack.peek().copyIntoGlobalPoseStack();

        LevelRenderer.renderLineBox(stack, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    protected void calculateWorldMatrices(FiguraModelPart part) {
        VIEW_MATRICES.setIdentity();

        FiguraMat4 posMat = part.savedPartToWorldMat.copy();
        FiguraMat4 worldToView = AvatarRenderer.worldToViewMatrix();
        VIEW_MATRICES.mulPoseMatrix(worldToView.toMatrix4f());
        VIEW_MATRICES.mulPoseMatrix(posMat.toMatrix4f());

        worldToView.free();
        posMat.free();
    }

    protected void savePivotTransform(ParentType parentType) {
        FiguraMat4 currentTransform = customizationStack.peek().getPositionMatrix();
        List<FiguraMat4> list = pivotCustomizations.computeIfAbsent(parentType, p -> new ArrayList<>());
        list.add(currentTransform); //CurrentTransform is a COPY, so it's okay to add it
    }

    protected FiguraMat4 partToWorldMatrices(PartCustomization cust) {
        FiguraMat4 customizePeek = customizationStack.peek().positionMatrix.copy();
        customizePeek.multiply(VIEW_TO_WORLD_MATRIX);
        FiguraVec3 piv = cust.getPivot();
        FiguraVec3 pos = cust.getPos();
        piv.subtract(pos);

        FiguraMat4 translation = FiguraMat4.of();
        translation.translate(piv);
        customizePeek.rightMultiply(translation);

        piv.free();
        pos.free();
        translation.free();

        return customizePeek;
    }

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(bufferSource, faceCount, remainingComplexity);
    }
}
