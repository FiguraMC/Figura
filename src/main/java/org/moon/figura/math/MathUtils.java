package org.moon.figura.math;

public class MathUtils {

    public static Object sizedVector(double... vals) {
        return switch (vals.length) {
            case 2 -> FiguraVec2.create(vals[0], vals[1]);
            case 3 -> FiguraVec3.create(vals);
            case 4 -> FiguraVec4.create(vals[0], vals[1], vals[2], vals[3]);
            case 5 -> FiguraVec5.create(vals[0], vals[1], vals[2], vals[3], vals[4]);
            case 6 -> FiguraVec6.create(vals);
            default -> throw new IllegalStateException("Cannot create vector of size: " + vals.length);
        };
    }


}
