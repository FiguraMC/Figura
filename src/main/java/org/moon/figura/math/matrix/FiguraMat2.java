package org.moon.figura.math.matrix;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

@LuaWhitelist
@LuaTypeDoc(
        name = "Matrix2",
        description = "matrix2"
)
public class FiguraMat2 implements CachedType {

    //Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    @LuaWhitelist
    public double v11, v12, v21, v22;

    @LuaFieldDoc(description = "matrix_n.vrc")
    public double vRC;

    private FiguraMat2() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraMat2> CACHE = CacheUtils.getCache(FiguraMat2::new);
    public void reset() {
        v12=v21 = 0;
        v11=v22 = 1;
    }
    public void free() {
        CACHE.offerOld(this);
    }
    public static FiguraMat2 of() {
        return CACHE.getFresh();
    }
    public static FiguraMat2 of(double n11, double n21,
                                double n12, double n22) {
        FiguraMat2 result = of();
        result.set(n11, n21, n12, n22);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------
    public double det() {
        return v11 * v22 - v12 * v21;
    }
    public FiguraMat2 copy() {
        FiguraMat2 result = of();
        result.set(this);
        return result;
    }
    public boolean equals(FiguraMat2 o) {
        return
                v11 == o.v11 && v12 == o.v12
                && v21 == o.v21 && v22 == o.v22;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraMat2 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "\n[  " + (float) v11 + ", " + (float) v12 +
                "\n   " + (float) v21 + ", " + (float) v22 +
                "  ]";
    }
    public FiguraVec2 getCol1() {
        return FiguraVec2.of(v11, v21);
    }
    public FiguraVec2 getCol2() {
        return FiguraVec2.of(v12, v22);
    }
    public FiguraVec2 getRow1() {
        return FiguraVec2.of(v11, v12);
    }
    public FiguraVec2 getRow2() {
        return FiguraVec2.of(v21, v22);
    }

    //----------------------------------------------------------------

    // STATIC CREATOR METHODS
    //----------------------------------------------------------------
    public static FiguraMat2 createScaleMatrix(double x, double y) {
        FiguraMat2 result = of();
        result.v11 = x;
        result.v22 = y;
        return result;
    }
    public static FiguraMat2 createRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat2 result = of();
        result.v11 = result.v22 = c;
        result.v12 = -s;
        result.v21 = s;
        return result;
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraMat2 o) {
        set(o.v11, o.v21, o.v12, o.v22);
    }
    public void set(double n11, double n21,
                    double n12, double n22) {
        v11 = n11;
        v12 = n12;
        v21 = n21;
        v22 = n22;
    }

    public void add(FiguraMat2 o) {
        add(o.v11, o.v21, o.v12, o.v22);
    }
    public void add(double n11, double n21,
                    double n12, double n22) {
        v11 += n11;
        v12 += n12;
        v21 += n21;
        v22 += n22;
    }

    public void subtract(FiguraMat2 o) {
        subtract(o.v11, o.v21, o.v12, o.v22);
    }
    public void subtract(double n11, double n21,
                    double n12, double n22) {
        v11 -= n11;
        v12 -= n12;
        v21 -= n21;
        v22 -= n22;
    }

    public void scale(double x, double y) {
        v11 *= x;
        v12 *= x;
        v21 *= y;
        v22 *= y;
    }

    public void rotate(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 - s*v21;
        double nv12 = c*v12 - s*v22;

        v21 = c*v21 + s*v11;
        v22 = c*v22 + s*v12;

        v11 = nv11;
        v12 = nv12;
    }

    public void multiply(FiguraMat2 o) {
        double nv11 = o.v11*v11+o.v12*v21;
        double nv12 = o.v11*v12+o.v12*v22;

        double nv21 = o.v21*v11+o.v22*v21;
        double nv22 = o.v21*v12+o.v22*v22;

        v11 = nv11;
        v12 = nv12;
        v21 = nv21;
        v22 = nv22;
    }

    //o is on the right.
    public void rightMultiply(FiguraMat2 o) {
        double nv11 = v11*o.v11+v12*o.v21;
        double nv12 = v11*o.v12+v12*o.v22;

        double nv21 = v21*o.v11+v22*o.v21;
        double nv22 = v21*o.v12+v22*o.v22;

        v11 = nv11;
        v12 = nv12;
        v21 = nv21;
        v22 = nv22;
    }

    public void transpose() {
        double temp = v12;
        v12 = v21;
        v21 = temp;
    }

    public void invert() {
        double det = det();
        set(
                v22 / det,
                v12 / det,
                v21 / det,
                v11 / det
        );
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraMat2 transposed() {
        FiguraMat2 result = copy();
        result.transpose();
        return result;
    }

    public FiguraMat2 inverted() {
        FiguraMat2 result = copy();
        result.invert();
        return result;
    }

    public FiguraMat2 plus(FiguraMat2 o) {
        FiguraMat2 result = copy();
        result.add(o);
        return result;
    }
    public FiguraMat2 plus(double n11, double n21,
                           double n12, double n22) {
        FiguraMat2 result = copy();
        result.add(n11, n21, n12, n22);
        return result;
    }

    public FiguraMat2 minus(FiguraMat2 o) {
        FiguraMat2 result = copy();
        result.subtract(o);
        return result;
    }
    public FiguraMat2 minus(double n11, double n21,
                            double n12, double n22) {
        FiguraMat2 result = copy();
        result.subtract(n11, n21, n12, n22);
        return result;
    }

    //Returns the product of the matrices, with "o" on the left.
    public FiguraMat2 times(FiguraMat2 o) {
        FiguraMat2 result = of();

        result.v11 = o.v11*v11+o.v12*v21;
        result.v12 = o.v11*v12+o.v12*v22;

        result.v21 = o.v21*v11+o.v22*v21;
        result.v22 = o.v21*v12+o.v22*v22;

        return result;
    }

    public FiguraVec2 times(FiguraVec2 vec) {
        FiguraVec2 result = FiguraVec2.of();
        result.x = v11*vec.x+v12*vec.y;
        result.y = v21*vec.x+v22*vec.y;
        return result;
    }

    // METAMETHODS
    //----------------------------------------------------------------
    @LuaWhitelist
    public static FiguraMat2 __add(@LuaNotNil FiguraMat2 arg1, @LuaNotNil FiguraMat2 arg2) {
        return arg1.plus(arg2);
    }
    @LuaWhitelist
    public static FiguraMat2 __sub(@LuaNotNil FiguraMat2 arg1, @LuaNotNil FiguraMat2 arg2) {
        return arg1.minus(arg2);
    }
    @LuaWhitelist
    public static FiguraMat2 __mul(@LuaNotNil FiguraMat2 arg1, @LuaNotNil FiguraMat2 arg2) {
        return arg2.times(arg1);
    }
    @LuaWhitelist
    public static FiguraVec2 __mul(@LuaNotNil FiguraMat2 arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg1.times(arg2);
    }
    @LuaWhitelist
    public static boolean __eq(@LuaNotNil FiguraMat2 arg1, @LuaNotNil FiguraMat2 arg2) {
        return arg1.equals(arg2);
    }
    @LuaWhitelist
    public static int __len(@LuaNotNil FiguraMat2 arg1) {
        return 2;
    }
    @LuaWhitelist
    public static String __tostring(@LuaNotNil FiguraMat2 arg1) {
        return arg1.toString();
    }
    @LuaWhitelist
    public static Object __index(@LuaNotNil FiguraMat2 arg1, @LuaNotNil String arg2) {
        return switch (arg2) {
            case "1" -> arg1.getCol1();
            case "2" -> arg1.getCol2();
            default -> null;
        };
    }

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.reset"
    )
    public static void reset(@LuaNotNil FiguraMat2 mat) {
        mat.reset();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.det"
    )
    public static double det(@LuaNotNil FiguraMat2 mat) {
        return mat.det();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.invert"
    )
    public static void invert(@LuaNotNil FiguraMat2 mat) {
        mat.invert();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.get_inverse"
    )
    public static FiguraMat2 getInverse(@LuaNotNil FiguraMat2 mat) {
        return mat.inverted();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.transpose"
    )
    public static void transpose(@LuaNotNil FiguraMat2 mat) {
        mat.transpose();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.get_transpose"
    )
    public static FiguraMat2 getTranspose(@LuaNotNil FiguraMat2 mat) {
        return mat.transposed();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraMat2.class, Double.class},
                    argumentNames = {"mat", "angle"}
            ),
            description = "matrix2.rotate"
    )
    public static void rotate(@LuaNotNil FiguraMat2 mat, @LuaNotNil Double degrees) {
        mat.rotate(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat2.class, FiguraVec2.class},
                            argumentNames = {"mat", "vec"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat2.class, Double.class, Double.class},
                            argumentNames = {"mat", "x", "y"}
                    )
            },
            description = "matrix_n.scale"
    )
    public static void scale(@LuaNotNil FiguraMat2 mat, Object x, Double y) {
        FiguraVec2 vec = LuaUtils.parseVec2("scale", x, y, 1, 1);
        mat.scale(vec.x, vec.y);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraMat2.class, Integer.class},
                    argumentNames = {"mat", "col"}
            ),
            description = "matrix2.get_column"
    )
    public static FiguraVec2 getColumn(@LuaNotNil FiguraMat2 mat, @LuaNotNil Integer column) {
        if (column <= 0 || column > 2) throw new IllegalArgumentException("Column " + column + " does not exist in a 2x2 matrix!");
        return switch (column) {
            case 1 -> mat.getCol1();
            case 2 -> mat.getCol2();
            default -> null;
        };
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraMat2.class, Integer.class},
                    argumentNames = {"mat", "row"}
            ),
            description = "matrix2.get_row"
    )
    public static FiguraVec2 getRow(@LuaNotNil FiguraMat2 mat, @LuaNotNil Integer row) {
        if (row <= 0 || row > 2) throw new IllegalArgumentException("Row " + row + " does not exist in a 2x2 matrix!");
        return switch (row) {
            case 1 -> mat.getRow1();
            case 2 -> mat.getRow2();
            default -> null;
        };
    }
}
