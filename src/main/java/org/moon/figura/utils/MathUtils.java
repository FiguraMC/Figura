package org.moon.figura.utils;

import org.moon.figura.math.vector.*;

public class MathUtils {

    public static Object sizedVector(double... vals) {
        return switch (vals.length) {
            case 2 -> FiguraVec2.of(vals[0], vals[1]);
            case 3 -> FiguraVec3.of(vals[0], vals[1], vals[2]);
            case 4 -> FiguraVec4.of(vals[0], vals[1], vals[2], vals[3]);
            case 5 -> FiguraVec5.of(vals[0], vals[1], vals[2], vals[3], vals[4]);
            case 6 -> FiguraVec6.of(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
            default -> throw new IllegalStateException("Cannot create vector of size: " + vals.length);
        };
    }

    public static boolean getBool(long number, int byteAt) {
        int byteVal = (int) Math.pow(2, byteAt);
        return (number & byteVal) == byteVal;
    }

    public static double modulus(double number, double mod) {
        return ((number % mod) + mod) % mod;
    }
}
