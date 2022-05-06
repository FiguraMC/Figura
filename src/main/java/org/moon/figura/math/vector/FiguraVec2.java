package org.moon.figura.math.vector;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.MathUtils;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector2",
        description = "A vector that holds 2 numbers. Can be created using functions in the \"vectors\" api."
)
public class FiguraVec2 implements CachedType {

    @LuaWhitelist
    public double x, y;

    private FiguraVec2() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraVec2> CACHE = CacheUtils.getCache(FiguraVec2::new);
    public void reset() {
        x = y = 0;
    }
    public void free() {
        CACHE.offerOld(this);
    }
    public static FiguraVec2 of() {
        return CACHE.getFresh();
    }
    public static FiguraVec2 of(double x, double y) {
        FiguraVec2 result = of();
        result.set(x, y);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------

    public double lengthSquared() {
        return x*x+y*y;
    }
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    public FiguraVec2 copy() {
        FiguraVec2 result = of();
        result.set(this);
        return result;
    }
    public double dot(FiguraVec2 o) {
        return x*o.x+y*o.y;
    }
    public boolean equals(FiguraVec2 o) {
        return x==o.x && y==o.y;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraVec2 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "{" + x + ", " + y + "}";
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraVec2 o) {
        set(o.x, o.y);
    }
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(FiguraVec2 o) {
        add(o.x, o.y);
    }
    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void subtract(FiguraVec2 o) {
        subtract(o.x, o.y);
    }
    public void subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
    }

    public void multiply(FiguraVec2 o) {
        multiply(o.x, o.y);
    }
    public void multiply(double x, double y) {
        this.x *= x;
        this.y *= y;
    }
    public void multiply(FiguraMat2 mat) {
        set(
                mat.v11*x+mat.v12*y,
                mat.v21*x+mat.v22*y
        );
    }

    public void divide(FiguraVec2 o) {
        divide(o.x, o.y);
    }
    public void divide(double x, double y) {
        this.x /= x;
        this.y /= y;
    }

    public void reduce(FiguraVec2 o) {
        reduce(o.x, o.y);
    } //modulo
    public void reduce(double x, double y) {
        this.x %= x;
        this.y %= y;
    }

    public void iDivide(FiguraVec2 o) {
        iDivide(o.x, o.y);
    }
    public void iDivide(double x, double y) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
    }

    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
    }
    public void normalize() {
        double l = length();
        if (l > 0)
            scale(1 / l);
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraVec2 plus(FiguraVec2 o) {
        return plus(o.x, o.y);
    }
    public FiguraVec2 plus(double x, double y) {
        FiguraVec2 result = copy();
        result.add(x, y);
        return result;
    }

    public FiguraVec2 minus(FiguraVec2 o) {
        return minus(o.x, o.y);
    }
    public FiguraVec2 minus(double x, double y) {
        FiguraVec2 result = copy();
        result.subtract(x, y);
        return result;
    }

    public FiguraVec2 times(FiguraVec2 o) {
        return times(o.x, o.y);
    }
    public FiguraVec2 times(double x, double y) {
        FiguraVec2 result = copy();
        result.multiply(x, y);
        return result;
    }
    public FiguraVec2 times(FiguraMat2 mat) {
        FiguraVec2 result = copy();
        result.multiply(mat);
        return result;
    }

    public FiguraVec2 dividedBy(FiguraVec2 o) {
        return dividedBy(o.x, o.y);
    }
    public FiguraVec2 dividedBy(double x, double y) {
        FiguraVec2 result = copy();
        result.divide(x, y);
        return result;
    }

    public FiguraVec2 mod(FiguraVec2 o) {
        return mod(o.x, o.y);
    }
    public FiguraVec2 mod(double x, double y) {
        FiguraVec2 result = copy();
        result.reduce(x, y);
        return result;
    }

    public FiguraVec2 iDividedBy(FiguraVec2 o) {
        return iDividedBy(o.x, o.y);
    }
    public FiguraVec2 iDividedBy(double x, double y) {
        FiguraVec2 result = copy();
        result.iDivide(x, y);
        return result;
    }

    public FiguraVec2 scaled(double factor) {
        FiguraVec2 result = copy();
        result.scale(factor);
        return result;
    }
    public FiguraVec2 normalized() {
        FiguraVec2 result = copy();
        result.normalize();
        return result;
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec2 __add(FiguraVec2 arg1, FiguraVec2 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __sub(FiguraVec2 arg1, FiguraVec2 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mul(FiguraVec2 arg1, FiguraVec2 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mul(FiguraVec2 arg1, Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mul(Double arg1, FiguraVec2 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec2 __div(FiguraVec2 arg1, FiguraVec2 arg2) {
        if (arg2.x == 0 || arg2.y == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __div(FiguraVec2 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mod(FiguraVec2 arg1, FiguraVec2 arg2) {
        if (arg2.x == 0 || arg2.y == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mod(FiguraVec2 arg1, Double arg2) {
        if (arg2== 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2, arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __idiv(FiguraVec2 arg1, FiguraVec2 arg2) {
        if (arg2.x == 0 || arg2.y == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __idiv(FiguraVec2 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2, arg2);
    }

    @LuaWhitelist
    public static boolean __eq(FiguraVec2 arg1, FiguraVec2 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __unm(FiguraVec2 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(FiguraVec2 arg1) {
        return 6;
    }

    @LuaWhitelist
    public static String __tostring(FiguraVec2 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(FiguraVec2 arg1, String arg2) {
        int len = arg2.length();
        if (len == 1) return switch(arg2) {
            case "1", "r" -> arg1.x;
            case "2", "g" -> arg1.y;
            default -> null;
        };

        if (len > 6)
            throw new IllegalArgumentException("Invalid swizzle: " + arg2);
        double[] vals = new double[len];
        for (int i = 0; i < len; i++)
            vals[i] = switch (arg2.charAt(i)) {
                case '1', 'x', 'r' -> arg1.x;
                case '2', 'y', 'g' -> arg1.y;
                case '_' -> 0;
                default -> throw new IllegalArgumentException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "Returns the length of this vector."
    )
    public static double length(FiguraVec2 arg) {
        return Math.sqrt(lengthSquared(arg));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "Returns the length of this vector squared. " +
                            "Suitable when you only care about relative " +
                            "lengths, because it avoids a square root."
    )
    public static double lengthSquared(FiguraVec2 arg) {
        return arg.dot(arg);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec2.class, FiguraVec2.class},
                    argumentNames = {"vec1", "vec2"},
                    returnType = Double.class
            ),
            description = "Returns the dot product of vec1 and vec2."
    )
    public static double dot(FiguraVec2 arg1, FiguraVec2 arg2) {
        return arg1.dot(arg2);
    }
}
