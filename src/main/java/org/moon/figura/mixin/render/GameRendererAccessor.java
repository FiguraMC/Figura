package org.moon.figura.mixin.render;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Accessor("EFFECTS")
    static ResourceLocation[] getEffects() {
        throw new AssertionError();
    }
}
