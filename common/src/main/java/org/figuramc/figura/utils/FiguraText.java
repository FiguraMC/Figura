package org.figuramc.figura.utils;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.figuramc.figura.FiguraMod;

public class FiguraText extends TranslatableContents {

    public FiguraText() {
        super(FiguraMod.MOD_ID, FiguraMod.MOD_NAME);
    }

    public FiguraText(String string) {
        super(FiguraMod.MOD_ID + "." + string);
    }

    public FiguraText(String string, Object... args) {
        super(FiguraMod.MOD_ID + "." + string, args);
    }

    public static MutableComponent of() {
        return MutableComponent.create(new FiguraText());
    }

    public static MutableComponent of(String string) {
        return MutableComponent.create(new FiguraText(string));
    }

    public static MutableComponent of(String string, Object... args) {
        return MutableComponent.create(new FiguraText(string, args));
    }
}