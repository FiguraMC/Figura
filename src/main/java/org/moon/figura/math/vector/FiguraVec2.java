package org.moon.figura.math.vector;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector2",
        description = "vector2"
)
public class FiguraVec2 extends FiguraVector<FiguraVec2> implements CachedType {

    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.x")
    public double x;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.y")
    public double y;

    private FiguraVec2() {}

    // CACHING METHODS
    //----------------------------------------------------------------

    private static final CacheUtils.Cache<FiguraVec2> CACHE = CacheUtils.getCache(FiguraVec2::new);

    @Override
    public void reset() {
        x = y = 0;
    }

    @Override
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

    @Override
    public double lengthSquared() {
        return x*x+y*y;
    }

    @Override
    public FiguraVec2 copy() {
        FiguraVec2 result = of();
        result.set(this);
        return result;
    }

    @Override
    public double dot(FiguraVec2 o) {
        return x*o.x+y*o.y;
    }

    @Override
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
        return "{" + (float) x + ", " + (float) y + "}";
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    @Override
    public void set(FiguraVec2 o) {
        set(o.x, o.y);
    }
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void add(FiguraVec2 o) {
        add(o.x, o.y);
    }
    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public void subtract(FiguraVec2 o) {
        subtract(o.x, o.y);
    }
    public void subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
    }

    @Override
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

    @Override
    public void divide(FiguraVec2 o) {
        divide(o.x, o.y);
    }
    public void divide(double x, double y) {
        this.x /= x;
        this.y /= y;
    }

    @Override
    public void reduce(FiguraVec2 o) {
        reduce(o.x, o.y);
    } //modulo
    public void reduce(double x, double y) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
    }

    @Override
    public void iDivide(FiguraVec2 o) {
        iDivide(o.x, o.y);
    }
    public void iDivide(double x, double y) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
    }

    @Override
    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec2 __add(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __sub(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mul(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mul(@LuaNotNil FiguraVec2 arg1, @LuaNotNil Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mul(@LuaNotNil Double arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec2 __div(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        if (arg2.x == 0 || arg2.y == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __div(@LuaNotNil FiguraVec2 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mod(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        if (arg2.x == 0 || arg2.y == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __mod(@LuaNotNil FiguraVec2 arg1, @LuaNotNil Double arg2) {
        if (arg2== 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        FiguraVec2 modulus = FiguraVec2.of(arg2, arg2);
        FiguraVec2 result = arg1.mod(modulus);
        modulus.free();
        return result;
    }

    @LuaWhitelist
    public static FiguraVec2 __idiv(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        if (arg2.x == 0 || arg2.y == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __idiv(@LuaNotNil FiguraVec2 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        FiguraVec2 divisor = FiguraVec2.of(arg2, arg2);
        FiguraVec2 result = arg1.iDividedBy(divisor);
        divisor.free();
        return result;
    }

    @LuaWhitelist
    public static boolean __eq(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec2 __unm(@LuaNotNil FiguraVec2 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(@LuaNotNil FiguraVec2 arg1) {
        return 2;
    }

    @LuaWhitelist
    public static boolean __lt(@LuaNotNil FiguraVec2 l, @LuaNotNil FiguraVec2 r) {
        return l.x < r.x && l.y < r.y;
    }

    @LuaWhitelist
    public static boolean __le(@LuaNotNil FiguraVec2 l, @LuaNotNil FiguraVec2 r) {
        return l.x <= r.x && l.y <= r.y;
    }

    @LuaWhitelist
    public static String __tostring(@LuaNotNil FiguraVec2 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(@LuaNotNil FiguraVec2 arg1, @LuaNotNil String arg2) {
        if (arg2 == null)
            return null;
        int len = arg2.length();
        if (len == 1) return switch(arg2) {
            case "1", "r" -> arg1.x;
            case "2", "g" -> arg1.y;
            default -> null;
        };

        if (len > 6)
            throw new LuaRuntimeException("Invalid swizzle: " + arg2);
        double[] vals = new double[len];
        for (int i = 0; i < len; i++)
            vals[i] = switch (arg2.charAt(i)) {
                case '1', 'x', 'r' -> arg1.x;
                case '2', 'y', 'g' -> arg1.y;
                case '_' -> 0;
                default -> throw new LuaRuntimeException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec2> __ipairs(@LuaNotNil FiguraVec2 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec2> iPairsIterator = new LuaIPairsIterator<>(FiguraVec2.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec2, String> __pairs(@LuaNotNil FiguraVec2 arg) {
        return pairsIterator;
    }
    private static final LuaPairsIterator<FiguraVec2, String> pairsIterator =
            new LuaPairsIterator<>(List.of("x", "y"), FiguraVec2.class, String.class);

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.reset"
    )
    public static void reset(FiguraVec2 vec) {
        vec.reset();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec2.class, Double.class, Double.class},
                    argumentNames = {"vec", "x", "y"}
            ),
            description = "vector_n.set"
    )
    public static void set(FiguraVec2 vec, Double x, Double y) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        vec.set(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.copy"
    )
    public static FiguraVec2 copy(FiguraVec2 vec) {
        return vec.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalize"
    )
    public static void normalize(FiguraVec2 vec) {
        vec.normalize();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalized"
    )
    public static FiguraVec2 normalized(FiguraVec2 vec) {
        return vec.normalized();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec2.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamp_length"
    )
    public static void clampLength(@LuaNotNil FiguraVec2 arg, Double minLength, Double maxLength) {
        arg.clampLength(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec2.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamped"
    )
    public static FiguraVec2 clamped(@LuaNotNil FiguraVec2 arg, Double minLength, Double maxLength) {
        return arg.clamped(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length"
    )
    public static double length(@LuaNotNil FiguraVec2 arg) {
        return arg.length();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(@LuaNotNil FiguraVec2 arg) {
        return arg.lengthSquared();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec2.class, FiguraVec2.class},
                    argumentNames = {"vec1", "vec2"}
            ),
            description = "vector_n.dot"
    )
    public static double dot(@LuaNotNil FiguraVec2 arg1, @LuaNotNil FiguraVec2 arg2) {
        return arg1.dot(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_rad"
    )
    public static FiguraVec2 toRad(@LuaNotNil FiguraVec2 vec) {
        return vec.toRad();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec2.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_deg"
    )
    public static FiguraVec2 toDeg(@LuaNotNil FiguraVec2 vec) {
        return vec.toDeg();
    }
}
