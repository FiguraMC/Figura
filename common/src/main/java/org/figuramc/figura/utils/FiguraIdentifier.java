package org.figuramc.figura.utils;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura.FiguraMod;

public class FiguraIdentifier extends ResourceLocation {

    public FiguraIdentifier(String string) {
        super(FiguraMod.MOD_ID, string);
    }

    public static String formatPath(String path) {
        return Util.sanitizeName(path, ResourceLocation::validPathChar);
    }
}
