package org.moon.figura.utils;

import net.minecraft.network.chat.TranslatableComponent;
import org.moon.figura.FiguraMod;

public class FiguraText extends TranslatableComponent {

    public FiguraText() {
        super(FiguraMod.MOD_ID);
    }

    public FiguraText(String string) {
        super(FiguraMod.MOD_ID + "." + string);
    }

    public FiguraText(String string, Object... args) {
        super(FiguraMod.MOD_ID + "." + string, args);
    }
}