package org.moon.figura.model.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.model.rendering.texture.FiguraTexture;

public class TestAvatarRenderer extends AvatarRenderer {

    private final FiguraBuffer buffer;
    private final FiguraTexture texture;
    private final RenderType renderLayer;

    public TestAvatarRenderer(Avatar avatar, CompoundTag avatarCompound) {
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

        texture = new FiguraTexture(((CompoundTag) avatarCompound.getList("textures", Tag.TAG_COMPOUND).get(0)).getByteArray("src"));

        renderLayer = RenderType.entityCutout(texture.textureID);
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
        buffer.pushToConsumer(consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
}
