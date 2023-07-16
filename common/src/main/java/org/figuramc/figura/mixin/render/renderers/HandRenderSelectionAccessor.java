package org.figuramc.figura.mixin.render.renderers;

import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.ItemInHandRenderer$HandRenderSelection")
public interface HandRenderSelectionAccessor {

    @Intrinsic
    @Accessor("renderMainHand")
    boolean renderMainHand();

    @Intrinsic
    @Accessor("renderOffHand")
    boolean renderOffHand();
}
