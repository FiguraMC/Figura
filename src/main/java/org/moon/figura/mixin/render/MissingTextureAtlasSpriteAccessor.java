package org.moon.figura.mixin.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.util.LazyLoadedValue;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MissingTextureAtlasSprite.class)
public interface MissingTextureAtlasSpriteAccessor {

    @Intrinsic
    @Accessor("MISSING_IMAGE_DATA")
    static LazyLoadedValue<NativeImage> getImageData() {
        throw new AssertionError();
    }
}
