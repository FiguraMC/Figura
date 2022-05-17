package org.moon.figura.utils;

import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.math.vector.FiguraVec3;

import java.awt.*;

public class ColorUtils {

    public enum Colors {
        FRAN_PINK(0xFF72B7),
        CHLOE_PURPLE(0xA672EF),
        MAYA_BLUE(0x00F0FF),
        SKYE_BLUE(0x99BBEE),
        LILY_RED(0xFF2400),

        CHEESE(0xF8C53A),

        LUA_LOG(0x5555FF),
        LUA_ERROR(0xFF5555),
        LUA_PING(0xAA55FF),

        DEFAULT(0x5AAAFF);

        public final int hex;
        public final FiguraVec3 vec;
        public final Style style;

        Colors(int hex) {
            this.hex = hex;
            this.vec = intToRGB(hex);
            this.style = Style.EMPTY.withColor(hex);
        }
    }

    /**
     * splits a color integer into its channels
     * @param color - integer to split
     * @param len - channels length
     * @return an int array of the split int
     */
    public static int[] split(int color, int len) {
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            int shift = (len * 8) - ((i + 1) * 8);
            array[i] = color >> shift & 0xFF;
        }

        return array;
    }

    /**
     * converts a rgb vector into an integer
     * @param rgb - a vector of 0 to 1
     * @return an int
     */
    public static int rgbToInt(FiguraVec3 rgb) {
        int hex = (int) (rgb.x * 0xFF);
        hex = (hex << 8) + (int) (rgb.y * 0xFF);
        hex = (hex << 8) + (int) (rgb.z * 0xFF);
        return hex;
    }

    /**
     * converts an integer into a rgb vector
     * @param color - an integer
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 intToRGB(int color) {
        int[] rgb = ColorUtils.split(color, 3);
        return FiguraVec3.of(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
    }

    /**
     * parses a user input hex string into a color
     * checks for custom colors
     * @param hex - the hex string
     * @return an integer
     */
    public static int userInputHex(String hex) {
        return switch (hex.toLowerCase()) {
            case "fran", "francielly", "bunny" -> Colors.FRAN_PINK.hex;
            case "chloe", "space" -> Colors.CHLOE_PURPLE.hex;
            case "maya", "devnull" -> Colors.MAYA_BLUE.hex;
            case "sky", "skye", "skylar" -> Colors.SKYE_BLUE.hex;
            case "lily", "foxes", "fox" -> Colors.LILY_RED.hex;
            case "cheese", "largecheese", "large cheese" -> Colors.CHEESE.hex;
            default -> rgbToInt(hexStringToRGB(hex, Colors.DEFAULT.vec));
        };
    }

    /**
     * parses a hex string into a vector
     * '#' is optional
     * return value is based on string length
     * len 3 uses a short hex string
     * any other length fills the missing hex values to 0
     * @param string - a hex string
     * @param fallback - a vector, used as fallback if failed to parse the hex string
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 hexStringToRGB(String string, FiguraVec3 fallback) {
        //parse hex color
        StringBuilder hex = new StringBuilder(string);

        if (hex.toString().startsWith("#")) hex = new StringBuilder(hex.substring(1));
        if (hex.length() < 6) {
            char[] bgChar = hex.toString().toCharArray();

            //special catch for 3
            if (hex.length() == 3)
                hex = new StringBuilder("" + bgChar[0] + bgChar[0] + bgChar[1] + bgChar[1] + bgChar[2] + bgChar[2]);
            else
                hex.append("0".repeat(6 - hex.toString().length()));
        }

        //return
        try {
            return intToRGB(Integer.parseInt(hex.substring(0, 6), 16));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /**
     * overload for {@link #hexStringToRGB(String, FiguraVec3)}
     * fallback is an empty vec3
     */
    public static FiguraVec3 hexStringToRGB(String string) {
        return hexStringToRGB(string, FiguraVec3.of());
    }

    /**
     * converts a hsv vector into a rgb vector
     * @param hsv - a vector of 0 to 1
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 hsvToRGB(FiguraVec3 hsv) {
        int hex = Color.HSBtoRGB((float) hsv.x, (float) hsv.y, (float) hsv.z);
        return intToRGB(hex);
    }

    /**
     * converts a rgb vector into a hsv vector
     * @param rgb - a vector of 0 to 1
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 rgbToHSV(FiguraVec3 rgb) {
        float[] hsv = Color.RGBtoHSB((int) (rgb.x * 255f), (int) (rgb.y * 255f), (int) (rgb.z * 255f), null);
        return FiguraVec3.of(hsv[0], hsv[1], hsv[2]);
    }

    public static int rainbow(double speed) {
        return rgbToInt(hsvToRGB(FiguraVec3.of((FiguraMod.ticks * speed) % 255 / 255f, 0.7f, 1f)));
    }
}
