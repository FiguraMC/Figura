package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SimpleTexture.class)
public interface SimpleTextureAccessor {
    @Invoker("doLoad")
    void figura$doLoad(NativeImage image, boolean blur, boolean clamp);
}
