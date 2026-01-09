package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import org.figuramc.figura.compat.ImmediatelyFastCompat;
import org.figuramc.figura.ducks.BakedGlyphAccessor;
import org.figuramc.figura.font.EmojiContainer;
import org.figuramc.figura.font.EmojiMetadata;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedSheetGlyph.class)
public abstract class BakedSheetGlyphMixin implements BakedGlyphAccessor {
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
    private float v0;
    @Shadow
    @Final
    private float v1;
    @Shadow @Final private float left;
    @Shadow @Final private float right;
    @Shadow @Final private float u1;
    @Unique
    EmojiMetadata figura$metadata;

    @Override
    public void figura$setupEmoji(@Nullable EmojiContainer container, int codepoint) {
        if (container != null) {
            figura$metadata = container.getLookup().getMetadata(codepoint);
        }
    }

    @Inject(method = "render(ZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;IZI)V", at = @At("HEAD"), cancellable = true)
    public void render(boolean italic, float x, float y, float z, Matrix4f matrix, VertexConsumer vertexConsumer, int color, boolean bold, int light, CallbackInfo ci) {
        if (figura$metadata == null) return;

        float h = this.up;
        float j = this.down;
        float k = y + h;
        float l = y + j;
        float m = italic ? 1.0f - 0.25f * h : 0f;
        float n = italic ? 1.0f - 0.25f * j : 0f;
        float q = bold ? 0.1F : 0.0F;

        final float singleWidth = 8f / ImmediatelyFastCompat.getFontWidthIMF();
        float shift = singleWidth * figura$metadata.getCurrentFrame();

        float u = u0 + shift;
        vertexConsumer.addVertex(matrix, x + m - q, k - q, z).setColor(color).setUv(u, this.v0).setLight(light);
        vertexConsumer.addVertex(matrix, x + n - q, l + q, z).setColor(color).setUv(u, this.v1).setLight(light);
        vertexConsumer.addVertex(matrix, x + figura$metadata.width + n + q, l + q, z).setColor(color).setUv(u + singleWidth, this.v1).setLight(light);
        vertexConsumer.addVertex(matrix, x + figura$metadata.width + m + q, k - q, z).setColor(color).setUv(u + singleWidth, this.v0).setLight(light);
        ci.cancel();
    }
}
