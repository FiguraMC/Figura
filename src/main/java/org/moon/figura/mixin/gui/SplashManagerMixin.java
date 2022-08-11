package org.moon.figura.mixin.gui;

import net.minecraft.client.resources.SplashManager;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashManager.class)
public class SplashManagerMixin {

    @Inject(at = @At("HEAD"), method = "getSplash", cancellable = true)
    public void init(CallbackInfoReturnable<String> cir) {
        if (!Config.EASTER_EGGS.asBool())
            return;

        if (FiguraMod.CHEESE_DAY) {
            cir.setReturnValue("LARGECHEESE!");
        } else { //b-days!!
            int month = FiguraMod.DATE.getMonthValue();
            int day = FiguraMod.DATE.getDayOfMonth();
            String bday = "Happy birthday ";

            switch (month) {
                case 1 -> {
                    if (day == 1) cir.setReturnValue(bday + "Lily!");
                }
                case 3 -> {
                    switch (day) {
                        case 5 -> cir.setReturnValue(bday + "Maya!");
                        case 24 -> cir.setReturnValue(bday + FiguraMod.MOD_NAME + "!");
                    }
                }
                case 7 -> {
                    if (day == 4) cir.setReturnValue(bday + "Skylar!");
                }
                case 9 -> {
                    if (day == 21) cir.setReturnValue(bday + "Fran!");
                }
            }
        }
    }
}
