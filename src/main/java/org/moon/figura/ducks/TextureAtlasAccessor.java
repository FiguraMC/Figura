package org.moon.figura.ducks;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface TextureAtlasAccessor {
	Map<ResourceLocation, TextureAtlasSprite> getTexturesByName();
	int getWidth();
	int getHeight(); 
}
