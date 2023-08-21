package org.figuramc.figura.mixin.font;

import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.figuramc.figura.ducks.BitmapProviderGlyphAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BitmapProvider.Glyph.class)
public class BitmapGlyphMixin implements BitmapProviderGlyphAccessor {
    @Final
    @Mutable
    @Shadow
    private int advance;

    @Override
    public void figura$setAdvance(int advance) {
        this.advance = advance;
    }
}
