package org.moon.figura.utils;

import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;

import java.awt.*;

public class ColorUtils {

    public enum Colors {
        FRAN_PINK(0xFF72AD, "fran", "francielly", "bunny"),
        CHLOE_PURPLE(0xA672EF, "chloe", "space"),
        MAYA_BLUE(0x00F0FF, "maya", "devnull"),
        SKYE_BLUE(0x99BBEE, "sky", "skye", "skylar"),
        LILY_RED(0xFF2400, "lily", "foxes", "fox"),

        CHEESE(0xF8C53A, "largecheese", "large cheese"),

        LUA_LOG(0x5555FF),
        LUA_ERROR(0xFF5555),
        LUA_PING(0xA155DA, "luna", "moff", "moth"),

        DEFAULT(0x5AAAFF),
        DISCORD(0x5865F2);

        public final int hex;
        public final FiguraVec3 vec;
        public final Style style;
        public final String[] alias;

        Colors(int hex, String... alias) {
            this.hex = hex;
            this.vec = intToRGB(hex);
            this.style = Style.EMPTY.withColor(hex);
            this.alias = alias;
        }

        public static Colors getColor(String s) {
            if (s == null)
                return null;

            for (Colors value : Colors.values()) {
                if (s.equalsIgnoreCase(value.name()))
                    return value;
                for (String alias : value.alias) {
                    if (s.equalsIgnoreCase(alias))
                        return value;
                }
            }
            return null;
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
    public static int userInputHex(String hex, FiguraVec3 vec) {
        Colors color = Colors.getColor(hex);
        return color != null ? color.hex : rgbToInt(hexStringToRGB(hex, vec));
    }

    /**
     * overload for {@link #userInputHex(String, FiguraVec3)}
     * fallback is the default color
     */
    public static int userInputHex(String hex) {
        return userInputHex(hex, Colors.DEFAULT.vec);
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
        if (string == null || string.isBlank())
            return fallback;

        //parse hex color
        StringBuilder hex = new StringBuilder(string);

        if (hex.toString().startsWith("#")) hex = new StringBuilder(hex.substring(1));

        //short hex
        if (hex.length() == 3) {
            char[] bgChar = hex.toString().toCharArray();
            hex = new StringBuilder("" + bgChar[0] + bgChar[0] + bgChar[1] + bgChar[1] + bgChar[2] + bgChar[2]);
        } else {
            hex.append("0".repeat(Math.max(6 - hex.length(), 0)));
        }

        //return
        try {
            return intToRGB(Integer.parseInt(hex.substring(0, 6), 16));
        } catch (Exception ignored) {
            return fallback;
        }
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

    /**
     * converts a rgb vector into a hex color string
     * note that the string do not contain the "#" prefix
     * @param rgb - a vector of 0 to 1
     * @return a hex color String
     */
    public static String rgbToHex(FiguraVec3 rgb) {
        String color = Integer.toHexString(ColorUtils.rgbToInt(rgb));
        return "0".repeat(Math.max(6 - color.length(), 0)) + color;
    }

    public static FiguraVec3 rainbow() {
        return rainbow(1d, 1d, 1d);
    }
    public static FiguraVec3 rainbow(double speed, double saturation, double light) {
        return rainbow(speed, 0d, saturation, light);
    }
    public static FiguraVec3 rainbow(double speed, double offset, double saturation, double light) {
        return hsvToRGB(FiguraVec3.of(((FiguraMod.ticks * speed) + offset) % 255 / 255f, saturation, light));
    }

    public static int rgbaToIntABGR(FiguraVec4 rgba) {
        int hex = (int) (rgba.w * 0xFF);
        hex = (hex << 8) + (int) (rgba.z * 0xFF);
        hex = (hex << 8) + (int) (rgba.y * 0xFF);
        hex = (hex << 8) + (int) (rgba.x * 0xFF);
        return hex;
    }

    public static FiguraVec4 abgrToRGBA(int color) {
        int[] rgb = ColorUtils.split(color, 4);
        return FiguraVec4.of(rgb[3] / 255f, rgb[2] / 255f, rgb[1] / 255f, rgb[0] / 255f);
    }
}
