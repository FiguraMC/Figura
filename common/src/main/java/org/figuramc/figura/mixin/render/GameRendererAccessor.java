package org.figuramc.figura.mixin.render;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
<<<<<<< HEAD:src/main/java/org/moon/figura/mixin/render/GameRendererAccessor.java
=======
import org.spongepowered.asm.mixin.Final;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/mixin/render/GameRendererAccessor.java
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Intrinsic
    @Accessor("EFFECTS")
    static ResourceLocation[] getEffects() {
        throw new AssertionError();
    }
}