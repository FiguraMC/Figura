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

import java.util.ArrayList;
import java.util.List;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);

    public ImmediateAvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
        super(avatar, avatarCompound);

        //Vertex data, read model parts
        List<FiguraImmediateBuffer.Builder> builders = new ArrayList<>();
        root = FiguraModelPart.read(avatarCompound.getCompound("models"), builders);
        root.customization.setScale(1d/16, 1d/16, 1d/16);
        root.customization.needsMatrixRecalculation = true;
        root.customization.setPrimaryRenderType("CUTOUT_NO_CULL");
        root.customization.setSecondaryRenderType("EMISSIVE");

        //Textures
        List<FiguraTextureSet> textureSets = new ArrayList<>();
        ListTag texturesList = avatarCompound.getList("textures", Tag.TAG_COMPOUND);
        for (int i = 0; i < texturesList.size(); i++) {
            CompoundTag tag = texturesList.getCompound(i);
            String name = tag.getString("name");
            byte[] mainData = tag.getByteArray("default");
            mainData = mainData.length == 0 ? null : mainData;
            byte[] emissiveData = tag.getByteArray("emissive");
            emissiveData = emissiveData.length == 0 ? null : emissiveData;
            textureSets.add(new FiguraTextureSet(name, mainData, emissiveData));
        }

        for (int i = 0; i < textureSets.size() && i < builders.size(); i++)
            buffers.add(builders.get(i).build(textureSets.get(i)));
    }


    @Override
    public void render() {
        //Push position and normal matrices
        PartCustomization customization = PartCustomization.of();
        FiguraMat4 posMat = entityToWorldMatrix(entity, tickDelta);
        FiguraMat4 worldToView = worldToViewMatrix();
        posMat.multiply(worldToView);
        FiguraMat3 normalMat = posMat.deaugmented();

        customization.positionMatrix.set(posMat);
        customization.normalMatrix.set(normalMat);

        //Free matrices after use
        posMat.free();
        worldToView.free();
        normalMat.free();

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
        renderPart(root);

        //Pop position and normal matrices
        for (FiguraImmediateBuffer buffer : buffers) {
            buffer.popCustomization();
            buffer.checkEmpty();
        }

    }

    private void renderPart(FiguraModelPart part) {
        for (FiguraImmediateBuffer buffer : buffers)
            part.customization.pushToBuffer(buffer);

        part.pushVerticesImmediate(this);
        for (FiguraModelPart child : part.children)
            renderPart(child);

        for (FiguraImmediateBuffer buffer : buffers)
            buffer.popCustomization();
    }

    public void pushFaces(int texIndex, int faceCount) {
        buffers.get(texIndex).pushVertices(bufferSource, light, OverlayTexture.NO_OVERLAY, faceCount);
    }
}
