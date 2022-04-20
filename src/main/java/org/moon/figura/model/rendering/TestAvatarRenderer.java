package org.moon.figura.model.rendering;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.model.rendering.texture.FiguraTexture;

public class TestAvatarRenderer extends AvatarRenderer {

    private final FiguraBuffer buffer;
    private final FiguraTexture texture;
    private final RenderLayer renderLayer;

    public TestAvatarRenderer(Avatar avatar, NbtCompound avatarCompound) {
        super(avatar, avatarCompound);
        buffer = FiguraBuffer.builder()
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

        texture = new FiguraTexture(((NbtCompound) avatarCompound.getList("textures", NbtElement.COMPOUND_TYPE).get(0)).getByteArray("src"));

        renderLayer = RenderLayer.getEntityCutout(texture.textureID);
    }

    @Override
    public void render() {
        //Position matrix
        FiguraMat4 matrix = entityToWorldMatrix(entity, tickDelta);
        FiguraMat4 worldToView = worldToViewMatrix();
        matrix.multiply(worldToView);
        buffer.setTransform(matrix);

        //Normal matrix
        FiguraMat3 normalMat = matrix.deaugmented();
        buffer.setNormalMat(normalMat);

        //Free matrices
        matrix.free();
        worldToView.free();
        normalMat.free();

        //Texture
        texture.registerAndUpload();

        //Push vertices
        VertexConsumer consumer = vcp.getBuffer(renderLayer);
        buffer.pushToConsumer(consumer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
    }
}
