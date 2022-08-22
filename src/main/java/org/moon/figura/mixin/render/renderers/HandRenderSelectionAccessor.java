package org.moon.figura.mixin.render.renderers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.ItemInHandRenderer$HandRenderSelection")
public interface HandRenderSelectionAccessor {

    @Accessor("renderMainHand")
    boolean renderMainHand();

    @Accessor("renderOffHand")
    boolean renderOffHand();
}
