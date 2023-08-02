package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.ducks.BakedGlyphAccessor;
import org.figuramc.figura.gui.Emojis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedGlyph.class)
public abstract class BakedGlyphMixin implements BakedGlyphAccessor {

    @Shadow
    @Final
    private float left;
    @Shadow
    @Final
    private float right;
    @Shadow
    @Final
    private float up;
    @Shadow
    @Final
    private float down;
    @Shadow
    @Final
    private float u0;
    @Shadow
    @Final
    private float u1;
    @Shadow
    @Final
    private float v0;
    @Shadow
    @Final
    private float v1;
    @Unique
    Emojis.EmojiContainer.Metadata figura$metadata;
    @Unique
    int figura$sheetWidth = 1;

    @Override
    public void figura$setupEmoji(ResourceLocation figura$resourceLocation) {
        Emojis.EmojiContainer container = Emojis.getContainer(figura$resourceLocation);
        if (container != null) {
            figura$metadata = container.getEmojiMetadata((int) (v0 * container.textureHeight));
            figura$sheetWidth = container.textureWidth;
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, CallbackInfo ci) {
        if (figura$metadata == null) return;

        float h = this.up - 3.0f;
        float j = this.down - 3.0f;
        float k = y + h;
        float l = y + j;
        float m = italic ? 1.0f - 0.25f * h : 0f;
        float n = italic ? 1.0f - 0.25f * j : 0f;

        final float singleWidth = 8f / 256.0f;
        float shift = singleWidth * figura$metadata.getCurrentFrame();

        float u = u0 + shift;
        vertexConsumer.vertex(matrix, x
 + m, k, 0.0f).color(red, green, blue, alpha).uv(u, this.v0).uv2(light).endVertex();
        vertexConsumer.vertex(matrix, x + n, l, 0.0f).color(red, green, blue, alpha).uv(u, this.v1).uv2(light).endVertex();
        vertexConsumer.vertex(matrix, x + 8 + n, l, 0.0f).color(red, green, blue, alpha).uv(u + singleWidth, this.v1).uv2(light).endVertex();
        vertexConsumer.vertex(matrix, x + 8 + m, k, 0.0f).color(red, green, blue, alpha).uv(u + singleWidth, this.v0).uv2(light).endVertex();
        ci.cancel();
    }
}
