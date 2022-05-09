package org.moon.figura.avatars.model.rendering;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.PartCustomization;
import org.moon.figura.avatars.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;

import java.util.ArrayList;
import java.util.List;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);
    private final int complexityLimit; //In faces

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
            buffers.add(builders.get(i).build(textureSets.get(i)));

        avatar.hasTexture = !texturesList.isEmpty();
    }

    public void clean() {
        for (FiguraImmediateBuffer buffer : buffers)
            buffer.clean();
    }

    @Override
    public void render() {
        //Push position and normal matrices
        PartCustomization customization = transformRoot();
        customization.visible = true;

        //Iterate and setup each buffer
        for (FiguraImmediateBuffer buffer : buffers) {
            //Push transform
            buffer.pushCustomization(customization);
            //Reset buffers
            buffer.clearBuffers();
            //Upload texture if necessary
            buffer.uploadTexIfNeeded();
        }

        //Free customization after use
        customization.free();

        //Render all model parts
        renderPart(root, new int[] {complexityLimit});

        //Pop position and normal matrices
        for (FiguraImmediateBuffer buffer : buffers) {
            buffer.popCustomization();
            buffer.checkEmpty();
        }
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

    private void renderPart(FiguraModelPart part, int[] remainingComplexity) {
        part.applyVanillaTransforms(vanillaModel);

        for (FiguraImmediateBuffer buffer : buffers)
            part.customization.pushToBuffer(buffer);

        part.pushVerticesImmediate(this, remainingComplexity);
        for (FiguraModelPart child : part.children)
            renderPart(child, remainingComplexity);

        for (FiguraImmediateBuffer buffer : buffers)
            buffer.popCustomization();

        part.resetVanillaTransforms();
    }

    public void pushFaces(int texIndex, int faceCount, int[] remainingComplexity) {
        buffers.get(texIndex).pushVertices(bufferSource, light, OverlayTexture.NO_OVERLAY, faceCount, remainingComplexity);
    }
}
