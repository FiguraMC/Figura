package org.moon.figura.model.rendering;

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

    private final List<FiguraImmediateBuffer> buffers = new ArrayList<>(0);

    public ImmediateAvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
        super(avatar, avatarCompound);

        List<FiguraImmediateBuffer.Builder> builders = new ArrayList<>();
        root = FiguraModelPart.read(avatarCompound.getCompound("models"), builders);
        root.transform.scale.set(1d/16, 1d/16, 1d/16);
        root.transform.needsMatrixRecalculation = true;

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

        for (int i = 0; i < textureSets.size() && i < builders.size(); i++) {
            buffers.add(builders.get(i).build(textureSets.get(i)));
        }
    }


    @Override
    public void render() {
        //Push position and normal matrices
        FiguraMat4 posMat = entityToWorldMatrix(entity, tickDelta);
        FiguraMat4 worldToView = worldToViewMatrix();
        posMat.multiply(worldToView);
        FiguraMat3 normalMat = posMat.deaugmented();

        //Iterate and setup each buffer
        for (FiguraImmediateBuffer buffer : buffers) {
            //Push transform
            buffer.pushTransform(posMat, normalMat);
            //Reset buffers
            buffer.clearBuffers();
            //Upload texture if necessary
            buffer.uploadTexIfNeeded();
        }

        //Free matrices after use
        posMat.free();
        worldToView.free();
        normalMat.free();

        //Render all model parts
        renderPart(root);

        //Pop position and normal matrices
        for (FiguraImmediateBuffer buffer : buffers) {
            buffer.popTransform();
            buffer.checkTransformStackEmpty();
        }

    }

    private void renderPart(FiguraModelPart part) {
        for (FiguraImmediateBuffer buffer : buffers)
            part.transform.pushToBuffer(buffer);

        part.pushVerticesImmediate(this);
        for (FiguraModelPart child : part.children)
            renderPart(child);

        for (FiguraImmediateBuffer buffer : buffers)
            buffer.popTransform();
    }

    public void pushFaces(int texIndex, int faceCount) {
        buffers.get(texIndex).pushVertices(bufferSource, light, OverlayTexture.NO_OVERLAY, faceCount);
    }
}
