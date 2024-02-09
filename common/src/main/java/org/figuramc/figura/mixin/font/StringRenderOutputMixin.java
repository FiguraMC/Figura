package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.ducks.extensions.StringRenderOutputExtension;
import org.figuramc.figura.model.rendering.texture.RenderTypes;
import org.spongepowered.asm.mixin.*;

@Mixin(Font.StringRenderOutput.class)
public abstract class StringRenderOutputMixin implements StringRenderOutputExtension {
    // This is super cursed, we have to interact with everything indirectly
    // Forge remaps this to field_238428_b_ but Optifine and Fabric leave it as this
    @Shadow(aliases = {"this$0", "field_238428_b_", "field_24240"})
    @Final Font this$0;

    @Unique
    public boolean polygonOffset$accept(int i, Style style, int j) {
        float n;
        float l;
        float h;
        float d;
        FontSet fontSet = ((FontAccessor)this$0).invokeGetFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(j);
        BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
        boolean bl = style.isBold();
        float f = ((StringRenderOutputAccessor) this).getA();
        TextColor textColor = style.getColor();
        if (textColor != null) {
            int k = textColor.getValue();
            d = (float)(k >> 16 & 0xFF) / 255.0f * ((StringRenderOutputAccessor) this).getDimFactor();
            h = (float)(k >> 8 & 0xFF) / 255.0f * ((StringRenderOutputAccessor) this).getDimFactor();
            l = (float)(k & 0xFF) / 255.0f * ((StringRenderOutputAccessor) this).getDimFactor();
        } else {
            d = ((StringRenderOutputAccessor) this).getR();
            h = ((StringRenderOutputAccessor) this).getG();
            l = ((StringRenderOutputAccessor) this).getB();
        }
        if (!(bakedGlyph instanceof EmptyGlyph)) {
            float m = bl ? glyphInfo.getBoldOffset() : 0.0f;
            n = ((StringRenderOutputAccessor) this).getDropShadow() ? glyphInfo.getShadowOffset() : 0.0f;
            FontSetAccessor fontSetAccessor = (FontSetAccessor) fontSet;
            VertexConsumer vertexConsumer = ((StringRenderOutputAccessor) this).getBufferSource().getBuffer(RenderTypes.FiguraRenderType.TEXT_POLYGON_OFFSET.apply(fontSetAccessor.getTextures().get(0).getName()));
            ((FontAccessor)this$0).invokeRenderChar(bakedGlyph, bl, style.isItalic(), m, ((StringRenderOutputAccessor) this).getX() + n, ((StringRenderOutputAccessor) this).getY() + n, ((StringRenderOutputAccessor) this).getPose(), vertexConsumer, d, h, l, f, ((StringRenderOutputAccessor) this).getPackedLightCoords());
        }
        float m = glyphInfo.getAdvance(bl);
        n = ((StringRenderOutputAccessor) this).getDropShadow() ? 1.0f : 0.0f;
        if (style.isStrikethrough()) {
            ((StringRenderOutputAccessor) this).invokeAddEffect(new BakedGlyph.Effect(((StringRenderOutputAccessor) this).getX() + n - 1.0f, ((StringRenderOutputAccessor) this).getY() + n + 4.5f, ((StringRenderOutputAccessor) this).getX() + n + m, ((StringRenderOutputAccessor) this).getY() + n + 4.5f - 1.0f, 0.01f, d, h, l, f));
        }
        if (style.isUnderlined()) {
            ((StringRenderOutputAccessor) this).invokeAddEffect(new BakedGlyph.Effect(((StringRenderOutputAccessor) this).getX() + n - 1.0f, ((StringRenderOutputAccessor) this).getY() + n + 9.0f, ((StringRenderOutputAccessor) this).getX() + n + m, ((StringRenderOutputAccessor) this).getY() + n + 9.0f - 1.0f, 0.01f, d, h, l, f));
        }

        ((StringRenderOutputAccessor) this).setX(((StringRenderOutputAccessor) this).getX() + m);
        return true;
    }

    @Unique
    public float polyonOffset$finish(int i, float f) {
        if (i != 0) {
            float g = (float)(i >> 24 & 0xFF) / 255.0f;
            float h = (float)(i >> 16 & 0xFF) / 255.0f;
            float j = (float)(i >> 8 & 0xFF) / 255.0f;
            float k = (float)(i & 0xFF) / 255.0f;
            ((StringRenderOutputAccessor) this).invokeAddEffect(new BakedGlyph.Effect(f - 1.0f, ((StringRenderOutputAccessor) this).getY() + 9.0f, ((StringRenderOutputAccessor) this).getX() + 1.0f, ((StringRenderOutputAccessor) this).getY() - 1.0f, 0.01f, h, j, k, g));
        }
        if (((StringRenderOutputAccessor) this).getEffects() != null) {
            FontSet fontSet = ((FontAccessor)this$0).invokeGetFontSet(Style.DEFAULT_FONT);
            BakedGlyph bakedGlyph = fontSet.whiteGlyph();
            FontSetAccessor fontSetAccessor = (FontSetAccessor) fontSet;
            VertexConsumer vertexConsumer = ((StringRenderOutputAccessor) this).getBufferSource().getBuffer(RenderTypes.FiguraRenderType.TEXT_POLYGON_OFFSET.apply(new ResourceLocation(fontSetAccessor.getName().getNamespace(), fontSetAccessor.getName().getPath() + "/" + fontSetAccessor.getTextures().size())));
            for (BakedGlyph.Effect effect : ((StringRenderOutputAccessor) this).getEffects()) {
                bakedGlyph.renderEffect(effect, ((StringRenderOutputAccessor) this).getPose(), vertexConsumer, ((StringRenderOutputAccessor) this).getPackedLightCoords());
            }
        }
        return ((StringRenderOutputAccessor) this).getX();
    }
}
