package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.figuramc.figura.ducks.extensions.FontExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Font.class)
public abstract class FontMixin implements FontExtension {
    @Shadow
    private static int adjustColor(int color) {
        throw new AssertionError();
    }

    @Shadow protected abstract FontSet getFontSet(ResourceLocation fontLocation);

    public void figura$drawInBatch8xOutline(FormattedCharSequence text, float x, float y, int color, int outlineColor, Matrix4f matrix, MultiBufferSource vertexConsumers, int light) {
        int i = adjustColor(outlineColor);
        Font fontInstance = (Font) (Object) this;
        Font.StringRenderOutput stringRenderOutput = fontInstance.new StringRenderOutput(vertexConsumers, 0.0f, 0.0f, i, false, matrix, false, light);
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                if (j == 0 && k == 0) continue;
                float[] fs = new float[]{x};
                int l2 = j;
                int m2 = k;
                text.accept((l, style, m) -> {
                    boolean bl = style.isBold();
                    FontSet fontSet = getFontSet(style.getFont());
                    GlyphInfo glyphInfo = fontSet.getGlyphInfo(m);
                    stringRenderOutput.x = fs[0] + (float)l2 * glyphInfo.getShadowOffset();
                    stringRenderOutput.y = y + (float)m2 * glyphInfo.getShadowOffset();
                    fs[0] = fs[0] + glyphInfo.getAdvance(bl);
                    return stringRenderOutput.accept(l, style.withColor(TextColor.fromRgb(i)), m);
                });
            }
        }
        Font.StringRenderOutput stringRenderOutput2 = fontInstance.new StringRenderOutput(vertexConsumers, x, y, adjustColor(color), false, matrix, false, light);
        text.accept(stringRenderOutput2);
        stringRenderOutput2.finish(0, x);
    }
}
