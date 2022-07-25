package org.moon.figura.math.vector;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector6",
        description = "vector6"
)
public class FiguraVec6 extends FiguraVector<FiguraVec6> implements CachedType {

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

    @Override
    public void reset() {
        x = y = z = w = t = h = 0;
    }

    @Override
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

    @Override
    public double lengthSquared() {
        return x*x+y*y+z*z+w*w+t*t+h*h;
    }

    @Override
    public FiguraVec6 copy() {
        FiguraVec6 result = of();
        result.set(this);
        return result;
    }

    @Override
    public double dot(FiguraVec6 o) {
        return x*o.x+y*o.y+z*o.z+w*o.w+t*o.t+h*o.h;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void reduce(FiguraVec6 o) {
        reduce(o.x, o.y, o.z, o.w, o.t, o.h);
    } //modulo
    public void reduce(double x, double y, double z, double w, double t, double h) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;
        this.w = ((this.w % w) + w) % w;
        this.t = ((this.t % t) + t) % t;
        this.h = ((this.h % h) + h) % h;
    }

    @Override
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

    @Override
    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
        this.t *= factor;
        this.h *= factor;
    }

    //----------------------------------------------------------------


    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec6 __add(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __sub(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mul(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mul(@LuaNotNil FiguraVec6 arg1, @LuaNotNil Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mul(@LuaNotNil Double arg1, @LuaNotNil FiguraVec6 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec6 __div(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0 || arg2.h == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __div(@LuaNotNil FiguraVec6 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mod(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0 || arg2.h == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __mod(@LuaNotNil FiguraVec6 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        FiguraVec6 modulus = FiguraVec6.of(arg2, arg2, arg2, arg2, arg2, arg2);
        FiguraVec6 result = arg1.mod(modulus);
        modulus.free();
        return result;
    }

    @LuaWhitelist
    public static FiguraVec6 __idiv(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0 || arg2.t == 0 || arg2.h == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __idiv(@LuaNotNil FiguraVec6 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        FiguraVec6 divisor = FiguraVec6.of(arg2, arg2, arg2, arg2, arg2, arg2);
        FiguraVec6 result = arg1.iDividedBy(divisor);
        divisor.free();
        return result;
    }

    @LuaWhitelist
    public static boolean __eq(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec6 __unm(@LuaNotNil FiguraVec6 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(@LuaNotNil FiguraVec6 arg1) {
        return 6;
    }

    @LuaWhitelist
    public static boolean __lt(@LuaNotNil FiguraVec6 l, @LuaNotNil FiguraVec6 r) {
        return l.x < r.x && l.y < r.y && l.z < r.z && l.w < r.w && l.t < r.t && l.h < r.h;
    }

    @LuaWhitelist
    public static boolean __le(@LuaNotNil FiguraVec6 l, @LuaNotNil FiguraVec6 r) {
        return l.x <= r.x && l.y <= r.y && l.z <= r.z && l.w <= r.w && l.t <= r.t && l.h <= r.h;
    }

    @LuaWhitelist
    public static String __tostring(@LuaNotNil FiguraVec6 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(@LuaNotNil FiguraVec6 arg1, @LuaNotNil String arg2) {
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
            throw new LuaRuntimeException("Invalid swizzle: " + arg2);
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
                default -> throw new LuaRuntimeException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec6> __ipairs(@LuaNotNil FiguraVec6 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec6> iPairsIterator = new LuaIPairsIterator<>(FiguraVec6.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec6, String> __pairs(@LuaNotNil FiguraVec6 arg) {
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
                    argumentNames = "vec"
            ),
            description = "vector_n.reset"
    )
    public static void reset(@LuaNotNil FiguraVec6 vec) {
        vec.reset();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec6.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"vec", "x", "y", "z", "w", "t", "h"}
            ),
            description = "vector_n.set"
    )
    public static void set(@LuaNotNil FiguraVec6 vec, Double x, Double y, Double z, Double w, Double t, Double h) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        if (t == null) t = 0d;
        if (h == null) h = 0d;
        vec.set(x, y, z, w, t, h);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.copy"
    )
    public static FiguraVec6 copy(@LuaNotNil FiguraVec6 vec) {
        return vec.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalize"
    )
    public static void normalize(@LuaNotNil FiguraVec6 vec) {
        vec.normalize();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalized"
    )
    public static FiguraVec6 normalized(@LuaNotNil FiguraVec6 vec) {
        return vec.normalized();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec6.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamp_length"
    )
    public static void clampLength(@LuaNotNil FiguraVec6 arg, Double minLength, Double maxLength) {
        arg.clampLength(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec6.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamped"
    )
    public static FiguraVec6 clamped(@LuaNotNil FiguraVec6 arg, Double minLength, Double maxLength) {
        return arg.clamped(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length"
    )
    public static double length(@LuaNotNil FiguraVec6 arg) {
        return arg.length();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(@LuaNotNil FiguraVec6 arg) {
        return arg.lengthSquared();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec6.class, FiguraVec6.class},
                    argumentNames = {"vec1", "vec2"}
            ),
            description = "vector_n.dot"
    )
    public static double dot(@LuaNotNil FiguraVec6 arg1, @LuaNotNil FiguraVec6 arg2) {
        return arg1.dot(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_rad"
    )
    public static FiguraVec6 toRad(@LuaNotNil FiguraVec6 vec) {
        return vec.toRad();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec6.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_deg"
    )
    public static FiguraVec6 toDeg(@LuaNotNil FiguraVec6 vec) {
        return vec.toDeg();
    }
}
