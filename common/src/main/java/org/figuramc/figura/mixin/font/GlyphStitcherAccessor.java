package org.figuramc.figura.mixin.font;

import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlyphStitcher.class)
public interface GlyphStitcherAccessor {
    @Accessor("texturePrefix")
    ResourceLocation getName();

}
