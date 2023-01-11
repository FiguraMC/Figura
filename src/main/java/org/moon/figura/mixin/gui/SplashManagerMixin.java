package org.moon.figura.mixin.gui;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ColorUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Calendar;
import java.util.Date;

@Mixin(SplashManager.class)
public class SplashManagerMixin {

    @Inject(at = @At("RETURN"), method = "getSplash")
    private void init(CallbackInfoReturnable<String> cir) {
        FiguraMod.splashText = null;
        if (!Config.EASTER_EGGS.asBool())
            return;

        Calendar calendar = FiguraMod.CALENDAR;
        calendar.setTime(new Date());

        String who = null;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        switch (calendar.get(Calendar.MONTH)) {
            case Calendar.JANUARY -> {
                if (day == 1) who = "Foxes";
            }
            case Calendar.MARCH -> {
                switch (day) {
                    case 5 -> who = "Limits";
                    case 24 -> who = FiguraMod.MOD_NAME;
                }
            }
            case Calendar.JULY -> {
                if (day == 4) who = "Skylar";
            }
            case Calendar.SEPTEMBER -> {
                if (day == 21) who = "Fran";
            }
        }

        if (who != null) {
            FiguraMod.splashText = Component.literal("Happy birthday " + who + " ")
                    .append(Badges.System.DEFAULT.badge.copy().withStyle(Style.EMPTY.withFont(Badges.FONT).withColor(ColorUtils.Colors.DEFAULT.hex)))
                    .append("!");
        }
    }
}
