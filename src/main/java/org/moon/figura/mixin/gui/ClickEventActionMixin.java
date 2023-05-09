package org.moon.figura.mixin.gui;

import net.minecraft.network.chat.ClickEvent;
import org.moon.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClickEvent.Action.class)
public class ClickEventActionMixin {

    @Inject(at = @At("HEAD"), method = "isAllowedFromServer", cancellable = true)
    private void isAllowedFromServer(CallbackInfoReturnable<Boolean> cir) {
        if (FiguraMod.parseMessages)
            cir.setReturnValue(true);
    }
}
