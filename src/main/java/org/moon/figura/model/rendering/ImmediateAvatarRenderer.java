package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.model.FiguraModelPart;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;

import java.util.ArrayList;
import java.util.List;

public class ImmediateAvatarRenderer extends AvatarRenderer {

    private FiguraImmediateBuffer buffer;
    private List<FiguraTextureSet> textureSets = new ArrayList<>(0);;

    public ImmediateAvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
        super(avatar, avatarCompound);

        FiguraImmediateBuffer.Builder builder = FiguraImmediateBuffer.builder();
        root = FiguraModelPart.read(avatarCompound.getCompound("models"), builder);
        buffer = builder.build();

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
    }


    @Override
    public void render() {
        //Push position and normal matrices
        FiguraMat4 posMat = entityToWorldMatrix(entity, tickDelta);
        FiguraMat4 worldToView = worldToViewMatrix();
        posMat.multiply(worldToView);
        FiguraMat3 normalMat = posMat.deaugmented();
        buffer.pushTransform(posMat, normalMat);

        //Free matrices after use
        posMat.free();
        worldToView.free();
        normalMat.free();

        //Textures
        for (FiguraTextureSet textureSet : textureSets)
            textureSet.uploadIfNeeded();

        //Render all model parts
        renderPart(root);

        //Pop position and normal matrices
        buffer.popTransform();
    }

    private void renderPart(FiguraModelPart part) {
        part.transform.pushToBuffer(buffer);
        part.pushVerticesImmediate(this);
        for (FiguraModelPart child : part.children)
            renderPart(child);
        buffer.popTransform();
    }

    public void pushFaces(int texIndex, int faceCount) {
        RenderType mainType = textureSets.get(texIndex).mainType;
        RenderType emissiveType = textureSets.get(texIndex).emissiveType;

        if (mainType != null) {
            VertexConsumer consumer = bufferSource.getBuffer(mainType);
            buffer.pushToConsumer(consumer, light, OverlayTexture.NO_OVERLAY, faceCount);
        }
        if (emissiveType != null) {
            VertexConsumer consumer = bufferSource.getBuffer(emissiveType);
            buffer.pushToConsumer(consumer, light, OverlayTexture.NO_OVERLAY, faceCount);
        }
    }
}
