package org.figuramc.figura.mixin.render;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TextureAtlas.class)
public interface TextureAtlasAccessor {
    @Intrinsic
    @Accessor("texturesByName")
    Map<ResourceLocation, TextureAtlasSprite> getTexturesByName();

    @Intrinsic
    @Invoker("getWidth")
    int getWidth();

    @Intrinsic
    @Invoker("getHeight")
    int getHeight();
}
