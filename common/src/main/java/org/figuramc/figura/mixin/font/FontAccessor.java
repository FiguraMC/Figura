package org.figuramc.figura.mixin.font;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontAccessor {
    @Invoker("getFontSet")
    FontSet figura$getFontSet(ResourceLocation resourceLocation);

    @Accessor("filterFishyGlyphs")
    boolean figura$shouldFilterFishyGlyphs();
}
