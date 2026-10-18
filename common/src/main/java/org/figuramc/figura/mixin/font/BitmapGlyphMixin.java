package org.figuramc.figura.mixin.font;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.figuramc.figura.ducks.BitmapProviderGlyphAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BitmapProvider.Glyph.class)
public class BitmapGlyphMixin implements BitmapProviderGlyphAccessor {
    @Final
    @Mutable
    @Shadow
    private int advance;

    @Unique
    private GlyphInfo figura$glyphInfo;

    @Override
    public void figura$setAdvance(int advance) {
        this.advance = advance;
        figura$glyphInfo = null;
    }

    @ModifyReturnValue(method = "info", at = @At("RETURN"))
    private GlyphInfo figura$stableGlyphInfo(GlyphInfo original) {
        // Delayed baking must receive the same identity registered with the stitcher.
        if (figura$glyphInfo == null)
            figura$glyphInfo = original;
        return figura$glyphInfo;
    }
}
