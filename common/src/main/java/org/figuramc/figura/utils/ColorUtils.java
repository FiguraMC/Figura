package org.figuramc.figura.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.jetbrains.annotations.Range;

import java.awt.*;
import java.util.Locale;

public class ColorUtils {

    public enum Colors {
        AWESOME_BLUE(0x5EA5FF),
        PURPLE(0xA672EF),
        FIGURA_BLUE(0x00F0FF),
        SOFT_BLUE(0x99BBEE),
        FIGURA_RED(0xFF2400),
        ORANGE(0xFFC400),

        CHEESE(0xF8C53A),

        LUA_LOG(0x5555FF),
        LUA_ERROR(0xFF5555),
        LUA_PING(0xA155DA),

        DEFAULT(0x5AAAFF),
        DISCORD(0x5865F2),
        KOFI(0x27AAE0),
        GITHUB(0xFFFFFF),
        MODRINTH(0x1BD96A),
        CURSEFORGE(0xF16436);

        public final int hex;
        public final FiguraVec3 vec;
        public final Style style;

        Colors(int hex) {
            this.hex = hex;
            this.vec = intToRGB(hex);
            this.style = Style.EMPTY.withColor(hex);
        }

        public static Colors getColor(String s) {
            if (s == null)
                return null;

            for (Colors value : Colors.values()) {
                if (s.equalsIgnoreCase(value.name()))
                    return value;
            }
            return null;
        }

        public static Colors random() {
            Colors[] colors = values();
            return colors[(int) (Math.random() * colors.length)];
        }
    }

    /**
     * splits a color integer into its channels
     *
     * @param color - integer to split
     * @param len   - channels length
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
     *
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
     *
     * @param color - an integer
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 intToRGB(int color) {
        int[] rgb = ColorUtils.split(color, 3);
        return FiguraVec3.of(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
    }

    /**
     * parses a user input hex string into a color
     * checks for custom and minecraft colors
     *
     * @param hex      - the hex string
     * @param fallback - a vector, used as fallback if failed to parse the hex string
     * @return a rgb vector of either the hex color or the fallback
     */
    public static FiguraVec3 userInputHex(String hex, FiguraVec3 fallback) {
        Colors color = Colors.getColor(hex);
        if (color != null)
            return color.vec;

        try {
            ChatFormatting formatting = ChatFormatting.valueOf(hex.toUpperCase(Locale.US));
            Integer i = formatting.getColor();
            if (i != null)
                return intToRGB(i);
        } catch (Exception ignored) {
        }

        return hexStringToRGB(hex, fallback);
    }

    /**
     * overload for {@link #userInputHex(String, FiguraVec3)}
     * fallback is the default color
     */
    public static FiguraVec3 userInputHex(String hex) {
        return userInputHex(hex, Colors.DEFAULT.vec);
    }

    /**
     * parses a hex string into a vector
     * '#' is optional
     * return value is based on string length
     * len 3 uses a short hex string
     * any other length fills the missing hex values to 0
     *
     * @param string   - a hex string
     * @param fallback - a vector, used as fallback if failed to parse the hex string
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 hexStringToRGB(String string, FiguraVec3 fallback) {
        if (string == null || string.isBlank())
            return fallback;

        // parse hex color
        StringBuilder hex = new StringBuilder(string);

        if (hex.toString().startsWith("#")) hex = new StringBuilder(hex.substring(1));

        // short hex
        if (hex.length() == 3) {
            char[] bgChar = hex.toString().toCharArray();
            hex = new StringBuilder("" + bgChar[0] + bgChar[0] + bgChar[1] + bgChar[1] + bgChar[2] + bgChar[2]);
        } else {
            hex.append("0".repeat(Math.max(6 - hex.length(), 0)));
        }

        // return
        try {
            return intToRGB(Integer.parseInt(hex.substring(0, 6), 16));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /**
     * converts a hsv vector into a rgb vector
     *
     * @param hsv - a vector of 0 to 1
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 hsvToRGB(FiguraVec3 hsv) {
        int hex = Color.HSBtoRGB((float) hsv.x, (float) hsv.y, (float) hsv.z);
        return intToRGB(hex);
    }

    /**
     * converts a rgb vector into a hsv vector
     *
     * @param rgb - a vector of 0 to 1
     * @return a vector of 0 to 1
     */
    public static FiguraVec3 rgbToHSV(FiguraVec3 rgb) {
        float[] hsv = Color.RGBtoHSB((int) (rgb.x * 255f), (int) (rgb.y * 255f), (int) (rgb.z * 255f), null);
        return FiguraVec3.of(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * something about a gamma curve and sRGB and color perception ???
     * good luck documenting this xd
     */
    public static double linearToRGB(double channel) {
        if (channel >= 0.0031308)
            return 1.055 * Math.pow(channel, 1.0 / 2.4) - 0.055;
        return 12.92 * channel;
    }

    /**
     * {@link #linearToRGB} but backwards
     */
    public static double RGBtoLinear(double channel) {
        if (channel >= 0.04045)
            return Math.pow((channel + 0.055) / 1.055, 2.4);
        return channel / 12.92;
    }

    /**
     * Convert a sRGB color to Oklab (a perceptual color space.)
     * Based on <a href="https://bottosson.github.io/posts/oklab/">public domain code.</a>
     *
     * @param rgb sRGB (non-linear)
     * @return oklab equivalent
     */
    public static FiguraVec3 RGBtoOklab(FiguraVec3 rgb) {
        double r = RGBtoLinear(rgb.x);
        double g = RGBtoLinear(rgb.y);
        double b = RGBtoLinear(rgb.z);
        double l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b;
        double m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b;
        double s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b;
        double l_ = Math.cbrt(l), m_ = Math.cbrt(m), s_ = Math.cbrt(s);
        return FiguraVec3.of(
                0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
                1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
                0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
        );
    }

    public static FiguraVec3 oklabToRGB(FiguraVec3 oklab) {
        double l_ = oklab.x + 0.3963377774 * oklab.y + 0.2158037573 * oklab.z;
        double m_ = oklab.x - 0.1055613458 * oklab.y - 0.0638541728 * oklab.z;
        double s_ = oklab.x - 0.0894841775 * oklab.y - 1.2914855480 * oklab.z;
        double l = l_ * l_ * l_;
        double m = m_ * m_ * m_;
        double s = s_ * s_ * s_;
        return FiguraVec3.of(
                +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
        ).applyFunc(ColorUtils::linearToRGB);
    }

    public static FiguraVec3 oklabToOklch(FiguraVec3 oklab) {
        double c = Math.hypot(oklab.y, oklab.z);
        double h = Math.toDegrees(Math.atan2(oklab.z, oklab.y));
        return FiguraVec3.of(oklab.x, c, h);
    }

    public static FiguraVec3 oklchToOklab(FiguraVec3 oklch) {
        double a = oklch.y * Math.cos(Math.toRadians(oklch.z));
        double b = oklch.y * Math.sin(Math.toRadians(oklch.z));
        return FiguraVec3.of(oklch.x, a, b);
    }

    /**
     * converts a rgb vector into a hex color string
     * note that the string do not contain the "#" prefix
     *
     * @param rgb - a vector of 0 to 1
     * @return a hex color String
     */
    public static String rgbToHex(FiguraVec3 rgb) {
        String color = Integer.toHexString(ColorUtils.rgbToInt(rgb));
        return "0".repeat(Math.max(6 - color.length(), 0)) + color;
    }

    public static int rgbaToIntABGR(FiguraVec4 rgba) {
        int hex = (int) (rgba.w * 0xFF);
        hex = (hex << 8) + (int) (rgba.z * 0xFF);
        hex = (hex << 8) + (int) (rgba.y * 0xFF);
        hex = (hex << 8) + (int) (rgba.x * 0xFF);
        return hex;
    }

    public static int rgbaToIntARGB(FiguraVec4 rgba) {
        int hex = (int) (rgba.w * 0xFF);
        hex = (hex << 8) + (int) (rgba.x * 0xFF);
        hex = (hex << 8) + (int) (rgba.y * 0xFF);
        hex = (hex << 8) + (int) (rgba.z * 0xFF);
        return hex;
    }

    // This actually seems to storing BGAR this whole file is a lie, but i will just play along...
    public static int intRGBAToIntARGB(int hexRGBA) {
        int green = (hexRGBA >> 16) & 0xFF;
        int blue = (hexRGBA >> 8) & 0xFF;
        int alpha = hexRGBA & 0xFF;
        int red = (hexRGBA >> 24) & 0xFF;

        return ((alpha << 24) | (red << 16) | (green << 8) | blue);
    }

    public static int rgbaToInt(FiguraVec4 rgba) {
        int hex = (int) (rgba.x * 0xFF);
        hex = (hex << 8) + (int) (rgba.y * 0xFF);
        hex = (hex << 8) + (int) (rgba.z * 0xFF);
        hex = (hex << 8) + (int) (rgba.w * 0xFF);
        return hex;
    }

    public static FiguraVec4 abgrToRGBA(int color) {
        int[] rgb = ColorUtils.split(color, 4);
        return FiguraVec4.of(rgb[3] / 255f, rgb[2] / 255f, rgb[1] / 255f, rgb[0] / 255f);
    }

    public static FiguraVec4 intToRGBA(int color) {
        int[] rgb = ColorUtils.split(color, 4);
        return FiguraVec4.of(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f, rgb[3] / 255f);
    }

    public static FiguraVec4 intToARGB(int color) {
        int[] rgb = ColorUtils.split(color, 4);
        return FiguraVec4.of(rgb[3] / 255f, rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class ColorTheme {
        private static final int N_STOPS = 11;
        // stop 0 is black & stop 10 is white
        // inbetweens are various lightnesses. try 5 or 6 for accents

        // generated by pinning L values for color stops then finding max C for full H coverage
        private static final double[] L = {.0, .2, .25, .3, .4, .5, .6, .7, .8, .9, 1.};
        //                                 0   .2    .25    .3    .4    .5    .6    .7    .8  .9    1.
        private static final double[] C = {.0, .034, .0425, .051, .068, .085, .102, .119, .1, .048, .0};

        static {
            //noinspection ConstantValue
            if (L.length != C.length || L.length != N_STOPS)
                throw new AssertionError(
                        "i would do anything for a static_assert (bad constant values in ColorTheme. complain to your local developers)");
        }

        public final double hue;

        public ColorTheme(double hue) {
            this.hue = hue;
        }

        /**
         * Returns a 'color stop'.
         *
         * @param idx index of color stop to generate
         * @return RGB
         */
        public FiguraVec3 stop(@Range(from = 0, to = N_STOPS) int idx) {
            return MathUtils.clamp(
                    ColorUtils.oklabToRGB(ColorUtils.oklchToOklab(FiguraVec3.of(L[idx], C[idx], hue))),
                    0d,
                    1d
            );
        }
    }

    static {
        // eager init ColorTheme so assertions get checked faster
        Class<ColorTheme> ignored = ColorTheme.class;
    }
}
