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
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.config.Config;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    private final int complexityLimit; //In faces

    private final PartCustomization.Stack customizationStack = new PartCustomization.Stack();

    public ImmediateAvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
        super(avatar, avatarCompound);

        //Get complexity limit from trust
        complexityLimit = TrustManager.get(avatar.owner).get(TrustContainer.Trust.COMPLEXITY);

        //Vertex data, read model parts
        List<FiguraImmediateBuffer.Builder> builders = new ArrayList<>();
        root = FiguraModelPart.read(avatarCompound.getCompound("models"), builders);

        //Textures
        List<FiguraTextureSet> textureSets = new ArrayList<>();
        ListTag texturesList = avatarCompound.getList("textures", Tag.TAG_COMPOUND);
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
        //Push position and normal matrices
        PartCustomization customization = transformRoot();
        customization.visible = true;

        //Push transform
        customizationStack.push(customization);

        //Iterate and setup each buffer
        for (FiguraImmediateBuffer buffer : buffers) {
            //Reset buffers
            buffer.clearBuffers();
            //Upload texture if necessary
            buffer.uploadTexIfNeeded();
        }

        //Free customization after use
        customization.free();

        //Set shouldRenderPivots
        //Idk if this is the intended way to get a boolean config value
        shouldRenderPivots = (Boolean) Config.RENDER_DEBUG_PARTS_PIVOT.value
                && Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes();

        //Render all model parts
        renderPart(root, new int[] {complexityLimit}, false);


        customizationStack.pop();
        checkEmpty();
    }

    private PartCustomization transformRoot() {
        PartCustomization customization = PartCustomization.of();

        customization.setPrimaryRenderType("CUTOUT_NO_CULL");
        customization.setSecondaryRenderType("EMISSIVE");

        double s = 1.0 / 16;
        customization.positionMatrix.scale(s, s, s);
        customization.positionMatrix.rotateZ(180);
        customization.positionMatrix.translate(0, 1.5, 0);
        customization.normalMatrix.rotateZ(180);

        FiguraMat4 posMat = FiguraMat4.fromMatrix4f(matrices.last().pose());
        FiguraMat3 normalMat = FiguraMat3.fromMatrix3f(matrices.last().normal());

        customization.positionMatrix.multiply(posMat);
        customization.normalMatrix.multiply(normalMat);

        posMat.free();
        normalMat.free();

        return customization;
    }

    public static final Predicate<FiguraModelPart> RENDER_ALL = part -> true;
    public static final Predicate<FiguraModelPart> RENDER_LEFT_ARM = part -> part.parentType == FiguraModelPart.ParentType.LeftArm;
    public static final Predicate<FiguraModelPart> RENDER_RIGHT_ARM = part -> part.parentType == FiguraModelPart.ParentType.RightArm;
    public static final Predicate<FiguraModelPart> RENDER_HEAD = part -> part.parentType == FiguraModelPart.ParentType.Head;

    public Predicate<FiguraModelPart> partRenderPredicate = RENDER_ALL;

    private static boolean shouldRenderPivots;
    private void renderPart(FiguraModelPart part, int[] remainingComplexity, boolean parentPassedPredicate) {
        part.applyVanillaTransforms(vanillaModel);

        part.customization.recalculate();

        //Store old visibility, but overwrite it in case we only want to render certain parts
        Boolean storedVisiblity = part.customization.visible;
        parentPassedPredicate |= partRenderPredicate.test(part);
        part.customization.visible = parentPassedPredicate;

        customizationStack.push(part.customization);

        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity, parentPassedPredicate);

        if (shouldRenderPivots)
            renderPivot(part);

        customizationStack.pop();

        //Reset to stored visibility
        part.customization.visible = storedVisiblity;

        part.resetVanillaTransforms();
    }

    private static PoseStack debugPoseStack = new PoseStack();
    private void renderPivot(FiguraModelPart part) {
        FiguraVec3 color = part.index == -1 ? ColorUtils.Colors.MAYA_BLUE.vec : ColorUtils.Colors.FRAN_PINK.vec;
        double boxSize = part.index == -1 ? 1.0/24 : 1.0/48;
        debugPoseStack.setIdentity();
        FiguraMat4 posMat = customizationStack.peek().getPositionMatrix();
        boxSize /= Math.cbrt(posMat.det());

        debugPoseStack.mulPoseMatrix(posMat.toMatrix4f());
        FiguraVec3 piv = part.customization.getPivot();
        debugPoseStack.translate(piv.x, piv.y, piv.z);

        piv.free();
        posMat.free();

        LevelRenderer.renderLineBox(debugPoseStack, bufferSource.getBuffer(RenderType.LINES),
                -boxSize, -boxSize, -boxSize,
                boxSize, boxSize, boxSize,
                (float) color.x, (float) color.y, (float) color.z, 1f);
    }

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(bufferSource, light, OverlayTexture.NO_OVERLAY, faceCount, remainingComplexity);
    }
}
