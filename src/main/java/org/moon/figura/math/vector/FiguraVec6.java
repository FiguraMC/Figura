package org.moon.figura.math.vector;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector6",
        description = "vector6"
)
public class FiguraVec6 implements CachedType {

    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.x")
    public double x;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.y")
    public double y;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.z")
    public double z;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.w")
    public double w;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.t")
    public double t;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.h")
    public double h;

    private FiguraVec6() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraVec6> CACHE = CacheUtils.getCache(FiguraVec6::new);
    public void reset() {
        x = y = z = w = t = h = 0;
    }
    public void free() {
        CACHE.offerOld(this);
    }
    public static FiguraVec6 of() {
        return CACHE.getFresh();
    }
    public static FiguraVec6 of(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = of();
        result.set(x, y, z, w, t, h);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------

    public double lengthSquared() {
        return x*x+y*y+z*z+w*w+t*t+h*h;
    }
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    public FiguraVec6 copy() {
        FiguraVec6 result = of();
        result.set(this);
        return result;
    }
    public double dot(FiguraVec6 o) {
        return x*o.x+y*o.y+z*o.z+w*o.w+t*o.t+h*o.h;
    }
    public boolean equals(FiguraVec6 o) {
        return x==o.x && y==o.y && z==o.z && w==o.w && t==o.t && h==o.h;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraVec6 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "{" + (float) x + ", " + (float) y + ", " + (float) z + ", " + (float) w + ", " + (float) t + ", " + (float) h + "}";
    }

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraVec6 o) {
        set(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public void set(double x, double y, double z, double w, double t, double h) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.t = t;
        this.h = h;
    }

    public void add(FiguraVec6 o) {
        add(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public void add(double x, double y, double z, double w, double t, double h) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.t += t;
        this.h += h;
    }

    public void subtract(FiguraVec6 o) {
        subtract(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public void subtract(double x, double y, double z, double w, double t, double h) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.t -= t;
        this.h -= h;
    }

    public void multiply(FiguraVec6 o) {
        multiply(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public void multiply(double x, double y, double z, double w, double t, double h) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.t *= t;
        this.h *= h;
    }

    public void divide(FiguraVec6 o) {
        divide(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public void divide(double x, double y, double z, double w, double t, double h) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        this.t /= t;
        this.h /= h;
    }

    public void reduce(FiguraVec6 o) {
        reduce(o.x, o.y, o.z, o.w, o.t, o.h);
    } //modulo
    public void reduce(double x, double y, double z, double w, double t, double h) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
        this.t %= t;
        this.h %= h;
    }

    public void iDivide(FiguraVec6 o) {
        iDivide(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public void iDivide(double x, double y, double z, double w, double t, double h) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
        this.z = Math.floor(this.z / z);
        this.w = Math.floor(this.w / w);
        this.t = Math.floor(this.t / t);
        this.h = Math.floor(this.h / h);
    }

    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
        this.t *= factor;
        this.h *= factor;
    }
    public void normalize() {
        double l = length();
        if (l > 0)
            scale(1 / l);
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraVec6 plus(FiguraVec6 o) {
        return plus(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public FiguraVec6 plus(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = copy();
        result.add(x, y, z, w, t, h);
        return result;
    }

    public FiguraVec6 minus(FiguraVec6 o) {
        return minus(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public FiguraVec6 minus(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = copy();
        result.subtract(x, y, z, w, t, h);
        return result;
    }

    public FiguraVec6 times(FiguraVec6 o) {
        return times(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public FiguraVec6 times(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = copy();
        result.multiply(x, y, z, w, t, h);
        return result;
    }

    public FiguraVec6 dividedBy(FiguraVec6 o) {
        return dividedBy(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public FiguraVec6 dividedBy(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = copy();
        result.divide(x, y, z, w, t, h);
        return result;
    }

    public FiguraVec6 mod(FiguraVec6 o) {
        return mod(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public FiguraVec6 mod(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = copy();
        result.reduce(x, y, z, w, t, h);
        return result;
    }

    public FiguraVec6 iDividedBy(FiguraVec6 o) {
        return iDividedBy(o.x, o.y, o.z, o.w, o.t, o.h);
    }
    public FiguraVec6 iDividedBy(double x, double y, double z, double w, double t, double h) {
        FiguraVec6 result = copy();
        result.iDivide(x, y, z, w, t, h);
        return result;
    }

    public FiguraVec6 scaled(double factor) {
        FiguraVec6 result = copy();
        result.scale(factor);
        return result;
    }
    public FiguraVec6 normalized() {
        FiguraVec6 result = copy();
        result.normalize();
        return result;
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec6 __add(FiguraVec6 arg1, FiguraVec6 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __sub(FiguraVec6 arg1, FiguraVec6 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mul(FiguraVec6 arg1, FiguraVec6 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mul(FiguraVec6 arg1, Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mul(Double arg1, FiguraVec6 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec6 __div(FiguraVec6 arg1, FiguraVec6 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0 || arg2.h == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __div(FiguraVec6 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mod(FiguraVec6 arg1, FiguraVec6 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0 || arg2.h == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mod(FiguraVec6 arg1, Double arg2) {
        if (arg2== 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2, arg2, arg2, arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __idiv(FiguraVec6 arg1, FiguraVec6 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0 || arg2.h == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __idiv(FiguraVec6 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2, arg2, arg2, arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static boolean __eq(FiguraVec6 arg1, FiguraVec6 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __unm(FiguraVec6 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(FiguraVec6 arg1) {
        return 6;
    }

    @LuaWhitelist
    public static String __tostring(FiguraVec6 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(FiguraVec6 arg1, String arg2) {
        if (arg2 == null)
            return null;
        int len = arg2.length();
        if (len == 1) return switch(arg2) {
            case "1", "r" -> arg1.x;
            case "2", "g" -> arg1.y;
            case "3", "b" -> arg1.z;
            case "4", "a" -> arg1.w;
            case "5" -> arg1.t;
            case "6" -> arg1.h;
            default -> null;
        };

        if (len > 6)
            throw new IllegalArgumentException("Invalid swizzle: " + arg2);
        double[] vals = new double[len];
        for (int i = 0; i < len; i++)
            vals[i] = switch (arg2.charAt(i)) {
                case '1', 'x', 'r' -> arg1.x;
                case '2', 'y', 'g' -> arg1.y;
                case '3', 'z', 'b' -> arg1.z;
                case '4', 'w', 'a' -> arg1.w;
                case '5', 't' -> arg1.t;
                case '6', 'h' -> arg1.h;
                case '_' -> 0;
                default -> throw new IllegalArgumentException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec6> __ipairs(FiguraVec6 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec6> iPairsIterator = new LuaIPairsIterator<>(FiguraVec6.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec6, String> __pairs(FiguraVec6 arg) {
        return pairsIterator;
    }
    private static final LuaPairsIterator<FiguraVec6, String> pairsIterator =
            new LuaPairsIterator<>(List.of("x", "y", "z", "w", "t", "h"), FiguraVec6.class, String.class);

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "vector_n.length"
    )
    public static double length(FiguraVec6 arg) {
        return Math.sqrt(lengthSquared(arg));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(FiguraVec6 arg) {
        return arg.dot(arg);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec6.class, FiguraVec6.class},
                    argumentNames = {"vec1", "vec2"},
                    returnType = Double.class
            ),
            description = "vector_n.dot"
    )
    public static double dot(FiguraVec6 arg1, FiguraVec6 arg2) {
        return arg1.dot(arg2);
    }
}
