package org.moon.figura.mixin.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(at = @At("HEAD"), method = "handleComponentClicked", cancellable = true)
    private void handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style != null && style.getClickEvent() instanceof TextUtils.FiguraClickEvent event) {
            event.onClick.run();
            cir.setReturnValue(true);
        }
    }
}
