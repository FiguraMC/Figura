package org.moon.figura.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void getHoverName(CallbackInfoReturnable<Component> cir) {
        if (Configs.EMOJIS.value > 0)
            cir.setReturnValue(Emojis.applyEmojis(cir.getReturnValue()));
    }
}
