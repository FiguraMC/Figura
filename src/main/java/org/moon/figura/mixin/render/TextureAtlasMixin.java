package org.moon.figura.mixin.render;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.ducks.TextureAtlasAccessor;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin implements TextureAtlasAccessor {
	@Unique
	int width;
	@Unique
	int height;

	@Intrinsic
	@Accessor
	abstract public Map<ResourceLocation, TextureAtlasSprite> getTexturesByName ();

	@Override public int getWidth () {
		return width;
	}

	@Override public int getHeight () {
		return height;
	}
	
	@Inject(
			method = "reload",
			at = @At(
					"HEAD"
			)
	)
	private void setDimensions (TextureAtlas.Preparations preparations, CallbackInfo info) {
		width = ((PreparationsAccessor) preparations).getWidth();
		height = ((PreparationsAccessor) preparations).getHeight();
	}
	
	@Mixin(TextureAtlas.Preparations.class)
	private interface PreparationsAccessor {
		@Accessor
		int getWidth();
		@Accessor
		int getHeight();
	}
}
