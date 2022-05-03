package org.moon.figura.utils;

import net.minecraft.network.chat.TranslatableComponent;
import org.moon.figura.FiguraMod;

public class FiguraText extends TranslatableComponent {

    public FiguraText(String string) {
        super(FiguraMod.MOD_ID + "." + string);
    }
}