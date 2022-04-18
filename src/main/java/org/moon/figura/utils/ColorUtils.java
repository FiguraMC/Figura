package org.moon.figura.utils;

import net.minecraft.text.Style;
import org.moon.figura.math.vector.FiguraVec3;

import java.awt.Color;

public class ColorUtils {

    public enum Colors {
        FRAN_PINK(0xFF72B7),
        LILY_RED(0xFF2400),
        MAYA_BLUE(0x0CE0CE),
        CHEESE(0xF8C53A),
        CHLOE_PURPLE(0xA672EF);

        public final int hex;
        public final FiguraVec3 vec;
        public final Style style;

        Colors(int hex) {
            this.hex = hex;
            this.vec = intToRGB(hex);
            this.style = Style.EMPTY.withColor(hex);
        }
    }

    public static int[] split(int value, int len) {
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            int shift = (len * 8) - ((i + 1) * 8);
            array[i] = value >> shift & 0xFF;
        }

        return array;
    }

    public static int rgbToInt(FiguraVec3 rgb) {
        int hex = (int) (rgb.x * 0xFF);
        hex = (hex << 8) + (int) (rgb.y * 0xFF);
        hex = (hex << 8) + (int) (rgb.z * 0xFF);
        return hex;
    }

    public static FiguraVec3 intToRGB(int color) {
        int[] rgb = ColorUtils.split(color, 3);
        return FiguraVec3.of(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
    }

    public static FiguraVec3 hexStringToRGB(String hex, FiguraVec3 fallback) {
        //parse #
        if (hex.startsWith("#")) hex = hex.substring(1);

        //return
        try {
            return intToRGB(Integer.parseInt(hex, 16));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public static FiguraVec3 hexStringToRGB(String string) {
        //parse hex color
        StringBuilder hex = new StringBuilder(string);

        if (hex.toString().startsWith("#")) hex = new StringBuilder(hex.substring(1));
        if (hex.length() < 6) {
            char[] bgChar = hex.toString().toCharArray();

            //special catch for 3
            if (hex.length() == 3)
                hex = new StringBuilder("" + bgChar[0] + bgChar[0] + bgChar[1] + bgChar[1] + bgChar[2] + bgChar[2]);
            else
                hex.append("0".repeat(Math.max(0, 6 - hex.toString().length())));
        }

        //return
        try {
            return intToRGB(Integer.parseInt(hex.toString(), 16));
        } catch (Exception ignored) {
            return FiguraVec3.of();
        }
    }

    public static FiguraVec3 hsvToRGB(FiguraVec3 hsv) {
        int hex = Color.HSBtoRGB((float) hsv.x, (float) hsv.y, (float) hsv.z);
        return intToRGB(hex);
    }
}
