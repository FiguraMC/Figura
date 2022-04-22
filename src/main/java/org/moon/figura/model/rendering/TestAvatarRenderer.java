package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.model.rendering.texture.FiguraTexture;

public class TestAvatarRenderer extends AvatarRenderer {

    private final FiguraImmediateBuffer buffer;
    private final FiguraTexture texture;
    private final RenderType renderLayer;

    public TestAvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
        super(avatar, avatarCompound);
        buffer = FiguraImmediateBuffer.builder()
                .vertex(1, 0, 0, 0, 1, 0, 0, -1)
                .vertex(0, 0, 0, 1, 1, 0, 0, -1)
                .vertex(0, 1, 0, 1, 0, 0, 0, -1)
                .vertex(1, 1, 0, 0, 0, 0, 0, -1)

                .vertex(0, 0, 1, 0, 1, 0, 0, 1)
                .vertex(1, 0, 1, 1, 1, 0, 0, 1)
                .vertex(1, 1, 1, 1, 0, 0, 0, 1)
                .vertex(0, 1, 1, 0, 0, 0, 0, 1)

                .vertex(1, 0, 1, 0, 1, 1, 0, 0)
                .vertex(1, 0, 0, 1, 1, 1, 0, 0)
                .vertex(1, 1, 0, 1, 0, 1, 0, 0)
                .vertex(1, 1, 1, 0, 0, 1, 0, 0)

                .vertex(0, 0, 0, 0, 1, -1, 0, 0)
                .vertex(0, 0, 1, 1, 1, -1, 0, 0)
                .vertex(0, 1, 1, 1, 0, -1, 0, 0)
                .vertex(0, 1, 0, 0, 0, -1, 0, 0)

                .vertex(0, 1, 1, 0, 1, 0, 1, 0)
                .vertex(1, 1, 1, 1, 1, 0, 1, 0)
                .vertex(1, 1, 0, 1, 0, 0, 1, 0)
                .vertex(0, 1, 0, 0, 0, 0, 1, 0)

                .vertex(0, 0, 0, 0, 1, 0, -1, 0)
                .vertex(1, 0, 0, 1, 1, 0, -1, 0)
                .vertex(1, 0, 1, 1, 0, 0, -1, 0)
                .vertex(0, 0, 1, 0, 0, 0, -1, 0)

                .build();

        texture = new FiguraTexture(((CompoundTag) avatarCompound.getList("textures", Tag.TAG_COMPOUND).get(1)).getByteArray("default"));

        renderLayer = RenderType.entityCutout(texture.textureID);
    }

    @Override
    public void render() {
        //Position matrix
        FiguraMat4 matrix = entityToWorldMatrix(entity, tickDelta);
        FiguraMat4 worldToView = worldToViewMatrix();
        matrix.multiply(worldToView);

        //Normal matrix
        FiguraMat3 normalMat = matrix.deaugmented();
        buffer.pushTransform(matrix, normalMat);

        //Free matrices
        matrix.free();
        worldToView.free();
        normalMat.free();

        //Texture
        texture.registerAndUpload();

        //Push vertices
        VertexConsumer consumer = bufferSource.getBuffer(renderLayer);
        buffer.pushToConsumer(consumer, light, OverlayTexture.NO_OVERLAY, 6);
        buffer.popTransform();
    }
}
