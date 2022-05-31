package org.moon.figura.avatars.model.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.FiguraModelPartReader;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.config.Config;
import org.moon.figura.ducks.LivingEntityRendererAccessor;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    private final int complexityLimit; //In faces

    private final PartCustomization.Stack customizationStack = new PartCustomization.Stack();

    public ImmediateAvatarRenderer(Avatar avatar) {
        super(avatar);

        //Get complexity limit from trust
        complexityLimit = TrustManager.get(avatar.owner).get(TrustContainer.Trust.COMPLEXITY);

        //Textures
        List<FiguraTextureSet> textureSets = new ArrayList<>();
        ListTag texturesList = avatar.nbt.getList("textures", Tag.TAG_COMPOUND);
        for (int i = 0; i < texturesList.size(); i++) {
            CompoundTag tag = texturesList.getCompound(i);
            String name = tag.getString("name");
            byte[] mainData = tag.getByteArray("main");
            if (mainData.length == 0)
                mainData = tag.getByteArray("normal");
            if (mainData.length == 0)
                mainData = tag.getByteArray("default");
            if (mainData.length == 0)
                mainData = null;
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

    public void clean() {
        customizationStack.fullClear();
        for (FiguraImmediateBuffer buffer : buffers)
            buffer.clean();
    }

    public void checkEmpty() {
        if (!customizationStack.isEmpty())
            throw new IllegalStateException("Pushed matrices without popping them!");
    }

    @Override
    public void render() {
        //Offset is NOT hard coded for one pose, this comes from LivingEntityRenderer.java.
//        commonRender(1.5010000467300415D);

        //Edit: apparently that number was bad, and it is actually correct to just use 1.5d
        commonRender(1.5d);
    }

    @Override
    public void renderWorldParts() {
        commonRender(0);
    }

    private void commonRender(double vertOffset) {
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
        shouldRenderPivots = config < 1 || !Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() ? 0 : config;

        //Free customization after use
        customization.free();

        //Render all model parts
        if (allowMatrixUpdate)
            viewToWorldMatrix = AvatarRenderer.worldToViewMatrix().inverted();

        int[] remainingComplexity = new int[] {complexityLimit};
        renderPart(root, remainingComplexity, currentFilterScheme.initialValue());
        avatar.complexity = complexityLimit - remainingComplexity[0];

        customizationStack.pop();
        checkEmpty();
    }

    private PartCustomization setupRootCustomization(double vertOffset) {
        PartCustomization customization = PartCustomization.of();

        customization.setPrimaryRenderType(FiguraTextureSet.RenderTypes.TRANSLUCENT);
        customization.setSecondaryRenderType(FiguraTextureSet.RenderTypes.EMISSIVE);

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
        return customization;
    }


    private static int shouldRenderPivots;
    private static FiguraMat4 viewToWorldMatrix = FiguraMat4.of();
    private void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean parentPassedPredicate) {
        if (entityRenderer != null) {
            part.applyVanillaTransforms(entityRenderer.getModel());
            part.applyVanillaTransforms(((LivingEntityRendererAccessor<?>) entityRenderer).figura$getElytraModel());
        }

        part.customization.recalculate();

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisibility = part.customization.visible;
        boolean thisPassedPredicate = currentFilterScheme.predicate().test(part, parentPassedPredicate);

        part.customization.visible = FiguraModelPart.getVisible(part) && thisPassedPredicate;
        customizationStack.push(part.customization);
        part.customization.visible = storedVisibility;

        //Right now, part.customization.positionMatrix contains a transformation from part space to view space.
        if (thisPassedPredicate && allowMatrixUpdate) {
            FiguraMat4 customizePeek = customizationStack.peek().positionMatrix.copy();
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

        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, thisPassedPredicate);

        if (thisPassedPredicate && (shouldRenderPivots > 1 || shouldRenderPivots == 1 && customizationStack.peek().visible))
            renderPivot(part);

        customizationStack.pop();

        part.resetVanillaTransforms();
    }

    private static final PoseStack DEBUG_POSE_STACK = new PoseStack();
    private void renderPivot(FiguraModelPart part) {
        //Index == -1 means it's a group
        FiguraVec3 color = part.index == -1 ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = part.index == -1 ? 1.0/16 : 1.0/32;
        DEBUG_POSE_STACK.setIdentity();
        FiguraMat4 posMat = part.savedPartToWorldMat.copy();
        boxSize /= Math.cbrt(posMat.det());

        FiguraMat4 worldToView = AvatarRenderer.worldToViewMatrix();
        DEBUG_POSE_STACK.mulPoseMatrix(worldToView.toMatrix4f());
        DEBUG_POSE_STACK.mulPoseMatrix(posMat.toMatrix4f());

        worldToView.free();
        posMat.free();

        LevelRenderer.renderLineBox(DEBUG_POSE_STACK, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(bufferSource, OverlayTexture.NO_OVERLAY, faceCount, remainingComplexity);
    }
}
