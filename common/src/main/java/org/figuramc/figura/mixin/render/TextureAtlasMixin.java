package org.figuramc.figura.mixin.render;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.ducks.TextureAtlasAccessor;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin implements TextureAtlasAccessor {
    private int atlasWidth;
    private int atlasHeight;
    @Intrinsic
    @Accessor("texturesByName")
    abstract Map<ResourceLocation, TextureAtlasSprite> getTextureField();

    public Map<ResourceLocation, TextureAtlasSprite> getTexturesByName() {
        return getTextureField();
    }

    @Override
    public int getWidth() {
        return atlasWidth;
    }

    @Override
    public int getHeight() {
        return atlasHeight;
    }

    @Inject(method = "reload", at = @At("HEAD"))
    private void captureWidthAndHeight(TextureAtlas.Preparations preparations, CallbackInfo ci) {
        this.atlasWidth = preparations.width;
        this.atlasHeight = preparations.height;
    }
}
