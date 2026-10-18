package org.figuramc.figura.mixin.font;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.UnbakedGlyph;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.figuramc.figura.ducks.BitmapProviderGlyphAccessor;
import org.figuramc.figura.ducks.GlyphStitcherExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FontSet.class)
public abstract class FontSetMixin {
    @Shadow @Final private GlyphStitcher stitcher;

    @WrapOperation(method = {"lambda$selectProviders$0", "computeGlyphInfo"},
            at = @At(value = "INVOKE", target =
                    "Lcom/mojang/blaze3d/font/GlyphProvider;getGlyph(I)Lcom/mojang/blaze3d/font/UnbakedGlyph;"))
    private UnbakedGlyph figura$prepareGlyph(GlyphProvider provider, int codePoint,
                                            Operation<UnbakedGlyph> original) {
        UnbakedGlyph glyph = original.call(provider, codePoint);
        if (glyph != null) {
            if (((GlyphStitcherAccessor) stitcher).getName().getNamespace().equals("figura")
                    && glyph instanceof BitmapProvider.Glyph)
                ((BitmapProviderGlyphAccessor) glyph).figura$setAdvance(8);
            ((GlyphStitcherExtension) stitcher).addCodePoint(glyph.info(), codePoint);
        }
        return glyph;
    }
}