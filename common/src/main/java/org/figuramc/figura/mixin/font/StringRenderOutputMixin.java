package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.ducks.extensions.StringRenderOutputExtension;
import org.figuramc.figura.model.rendering.texture.RenderTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(Font.StringRenderOutput.class)
public abstract class StringRenderOutputMixin implements StringRenderOutputExtension {
    @Shadow @Final private float dimFactor;
    @Shadow @Final MultiBufferSource bufferSource;
    @Shadow @Final private boolean dropShadow;
    @Shadow @Final private float r;
    @Shadow @Final private float g;
    @Shadow @Final private float b;
    @Shadow @Final private float a;
    @Shadow public float x;
    @Shadow public float y;
    @Shadow @Final private Matrix4f pose;
    @Shadow @Final private int packedLightCoords;

    @Shadow protected abstract void addEffect(BakedGlyph.Effect effect);


    @Shadow private @Nullable List<BakedGlyph.Effect> effects;

    @Final @Shadow Font field_24240;

    @Unique
    public boolean polygonOffset$accept(int i, Style style, int j) {
        float n;
        float l;
        float h;
        float g;
        FontSet fontSet = ((FontAccessor)field_24240).invokeGetFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(j);
        BakedGlyph bakedGlyph = style.isObfuscated() && j != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(j);
        boolean bl = style.isBold();
        float f = this.a;
        TextColor textColor = style.getColor();
        if (textColor != null) {
            int k = textColor.getValue();
            g = (float)(k >> 16 & 0xFF) / 255.0f * this.dimFactor;
            h = (float)(k >> 8 & 0xFF) / 255.0f * this.dimFactor;
            l = (float)(k & 0xFF) / 255.0f * this.dimFactor;
        } else {
            g = this.r;
            h = this.g;
            l = this.b;
        }
        if (!(bakedGlyph instanceof EmptyGlyph)) {
            float m = bl ? glyphInfo.getBoldOffset() : 0.0f;
            n = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0f;
            FontSetAccessor fontSetAccessor = (FontSetAccessor) fontSet;
            VertexConsumer vertexConsumer = this.bufferSource.getBuffer(RenderTypes.FiguraRenderType.TEXT_POLYGON_OFFSET.apply(fontSetAccessor.getTextures().get(0).getName()));
            ((FontAccessor)field_24240).invokeRenderChar(bakedGlyph, bl, style.isItalic(), m, this.x + n, this.y + n, this.pose, vertexConsumer, g, h, l, f, this.packedLightCoords);
        }
        float m = glyphInfo.getAdvance(bl);
        n = this.dropShadow ? 1.0f : 0.0f;
        if (style.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0f, this.y + n + 4.5f, this.x + n + m, this.y + n + 4.5f - 1.0f, 0.01f, g, h, l, f));
        }
        if (style.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(this.x + n - 1.0f, this.y + n + 9.0f, this.x + n + m, this.y + n + 9.0f - 1.0f, 0.01f, g, h, l, f));
        }

        this.x += m;
        return true;
    }

    @Unique
    public float polyonOffset$finish(int i, float f) {
        if (i != 0) {
            float g = (float)(i >> 24 & 0xFF) / 255.0f;
            float h = (float)(i >> 16 & 0xFF) / 255.0f;
            float j = (float)(i >> 8 & 0xFF) / 255.0f;
            float k = (float)(i & 0xFF) / 255.0f;
            this.addEffect(new BakedGlyph.Effect(f - 1.0f, this.y + 9.0f, this.x + 1.0f, this.y - 1.0f, 0.01f, h, j, k, g));
        }
        if (this.effects != null) {
            FontSet fontSet = ((FontAccessor)field_24240).invokeGetFontSet(Style.DEFAULT_FONT);
            BakedGlyph bakedGlyph = fontSet.whiteGlyph();
            FontSetAccessor fontSetAccessor = (FontSetAccessor) fontSet;
            VertexConsumer vertexConsumer = this.bufferSource.getBuffer(RenderTypes.FiguraRenderType.TEXT_POLYGON_OFFSET.apply(new ResourceLocation(fontSetAccessor.getName().getNamespace(), fontSetAccessor.getName().getPath() + "/" + fontSetAccessor.getTextures().size())));
            for (BakedGlyph.Effect effect : this.effects) {
                bakedGlyph.renderEffect(effect, this.pose, vertexConsumer, this.packedLightCoords);
            }
        }
        return this.x;
    }
}
