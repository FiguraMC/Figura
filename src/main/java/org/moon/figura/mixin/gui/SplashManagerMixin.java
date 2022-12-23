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
        if (!Config.EASTER_EGGS.asBool())
            return;

        Calendar calendar = FiguraMod.CALENDAR;
        calendar.setTime(new Date());

        String bday = "Happy birthday ";
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        switch (calendar.get(Calendar.MONTH)) {
            case Calendar.JANUARY -> {
                if (day == 1) cir.setReturnValue(bday + "Foxes!");
            }
            case Calendar.MARCH -> {
                switch (day) {
                    case 5 -> cir.setReturnValue(bday + "Limits!");
                    case 24 -> cir.setReturnValue(bday + FiguraMod.MOD_NAME + "!");
                }
            }
            case Calendar.APRIL -> {
                if (day == 1) cir.setReturnValue("LARGECHEESE!");
            }
            case Calendar.JULY -> {
                if (day == 4) cir.setReturnValue(bday + "Skylar!");
            }
            case Calendar.SEPTEMBER -> {
                if (day == 21) cir.setReturnValue(bday + "Fran!");
            }
        }
    }
}
