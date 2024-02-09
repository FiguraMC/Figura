package org.figuramc.figura.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura.ducks.BakedQuadAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadAccessor {
    @Shadow @Final protected TextureAtlasSprite sprite;

    @Override
    public TextureAtlasSprite figura$getSprite() {
        return sprite;
    }
}
