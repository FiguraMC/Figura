package org.moon.figura.mixin.gui;

import net.minecraft.client.resources.SplashManager;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Calendar;
import java.util.Date;

@Mixin(SplashManager.class)
public class SplashManagerMixin {

    @Inject(at = @At("HEAD"), method = "getSplash", cancellable = true)
    public void init(CallbackInfoReturnable<String> cir) {
        if (!(boolean) Config.EASTER_EGGS.value)
            return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (FiguraMod.CHEESE_DAY) {
            cir.setReturnValue("LARGECHEESE!");
        } else { //b-days!!
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
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
