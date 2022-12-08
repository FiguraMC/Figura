package org.moon.figura.utils;

import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;

public class FiguraIdentifier extends ResourceLocation {

    public FiguraIdentifier(String string) {
        super(FiguraMod.MOD_ID, string);
    }

    public static String formatPath(String path) {
        return path.replaceAll("[^a-z\\d/._-]", "_");
    }
}
