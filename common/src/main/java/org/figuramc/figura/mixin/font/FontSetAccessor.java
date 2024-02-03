package org.figuramc.figura.mixin.font;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(FontSet.class)
public interface FontSetAccessor {
    @Intrinsic
    @Accessor("name")
    ResourceLocation getName();

    @Intrinsic
    @Accessor("textures")
    List<FontTexture> getTextures();
}
