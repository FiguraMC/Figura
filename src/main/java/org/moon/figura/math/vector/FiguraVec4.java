package org.moon.figura.math.vector;

import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector4",
        description = "vector4"
)
public class FiguraVec4 extends FiguraVector<FiguraVec4> implements CachedType {

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

    private FiguraVec4() {}

    // CACHING METHODS
    //----------------------------------------------------------------

    private static final CacheUtils.Cache<FiguraVec4> CACHE = CacheUtils.getCache(FiguraVec4::new);

    @Override
    public void reset() {
        x = y = z = w = 0;
    }

    @Override
    public void free() {
        CACHE.offerOld(this);
    }

    public static FiguraVec4 of() {
        return CACHE.getFresh();
    }

    public static FiguraVec4 of(double x, double y, double z, double w) {
        FiguraVec4 result = of();
        result.set(x, y, z, w);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------

    @Override
    public double lengthSquared() {
        return x*x+y*y+z*z+w*w;
    }

    @Override
    public FiguraVec4 copy() {
        FiguraVec4 result = of();
        result.set(this);
        return result;
    }

    @Override
    public double dot(FiguraVec4 o) {
        return x*o.x+y*o.y+z*o.z+w*o.w;
    }

    @Override
    public boolean equals(FiguraVec4 o) {
        return x==o.x && y==o.y && z==o.z && w==o.w;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraVec4 o)
            return equals(o);
        return false;
    }

    @Override
    public String toString() {
        return "{" + (float) x + ", " + (float) y + ", " + (float) z + ", " + (float) w + "}";
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    @Override
    public void set(FiguraVec4 o) {
        set(o.x, o.y, o.z, o.w);
    }
    public void set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Override
    public void add(FiguraVec4 o) {
        add(o.x, o.y, o.z, o.w);
    }
    public void add(double x, double y, double z, double w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
    }

    @Override
    public void subtract(FiguraVec4 o) {
        subtract(o.x, o.y, o.z, o.w);
    }
    public void subtract(double x, double y, double z, double w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
    }

    @Override
    public void multiply(FiguraVec4 o) {
        multiply(o.x, o.y, o.z, o.w);
    }
    public void multiply(double x, double y, double z, double w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
    }
    public void multiply(FiguraMat4 mat) {
        set(
                mat.v11*x+mat.v12*y+mat.v13*z+mat.v14*w,
                mat.v21*x+mat.v22*y+mat.v23*z+mat.v24*w,
                mat.v31*x+mat.v32*y+mat.v33*z+mat.v34*w,
                mat.v41*x+mat.v42*y+mat.v43*z+mat.v44*w
        );
    }

    @Override
    public void divide(FiguraVec4 o) {
        divide(o.x, o.y, o.z, o.w);
    }
    public void divide(double x, double y, double z, double w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
    }

    @Override
    public void reduce(FiguraVec4 o) {
        reduce(o.x, o.y, o.z, o.w);
    } //modulo
    public void reduce(double x, double y, double z, double w) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;
        this.w = ((this.w % w) + w) % w;

        if (x < 0) this.x -= x;
        if (y < 0) this.y -= y;
        if (z < 0) this.z -= z;
        if (w < 0) this.w -= w;
    }

    @Override
    public void iDivide(FiguraVec4 o) {
        iDivide(o.x, o.y, o.z, o.w);
    }
    public void iDivide(double x, double y, double z, double w) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
        this.z = Math.floor(this.z / z);
        this.w = Math.floor(this.w / w);
    }

    @Override
    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec4 __add(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __sub(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mul(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mul(@LuaNotNil FiguraVec4 arg1, @LuaNotNil Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mul(@LuaNotNil Double arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec4 __div(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __div(@LuaNotNil FiguraVec4 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mod(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mod(@LuaNotNil FiguraVec4 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        FiguraVec4 modulus = FiguraVec4.of(arg2, arg2, arg2, arg2);
        FiguraVec4 result = arg1.mod(modulus);
        modulus.free();
        return result;
    }

    @LuaWhitelist
    public static FiguraVec4 __idiv(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __idiv(@LuaNotNil FiguraVec4 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        FiguraVec4 divisor = FiguraVec4.of(arg2, arg2, arg2, arg2);
        FiguraVec4 result = arg1.iDividedBy(divisor);
        divisor.free();
        return result;
    }

    @LuaWhitelist
    public static boolean __eq(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __unm(@LuaNotNil FiguraVec4 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(@LuaNotNil FiguraVec4 arg1) {
        return 4;
    }

    @LuaWhitelist
    public static String __tostring(@LuaNotNil FiguraVec4 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(@LuaNotNil FiguraVec4 arg1, @LuaNotNil String arg2) {
        if (arg2 == null)
            return null;
        int len = arg2.length();
        if (len == 1) return switch(arg2) {
            case "1", "r" -> arg1.x;
            case "2", "g" -> arg1.y;
            case "3", "b" -> arg1.z;
            case "4", "a" -> arg1.w;
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
                case '_' -> 0;
                default -> throw new LuaRuntimeException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec4> __ipairs(@LuaNotNil FiguraVec4 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec4> iPairsIterator = new LuaIPairsIterator<>(FiguraVec4.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec4, String> __pairs(@LuaNotNil FiguraVec4 arg) {
        return pairsIterator;
    }
    private static final LuaPairsIterator<FiguraVec4, String> pairsIterator =
            new LuaPairsIterator<>(List.of("x", "y", "z", "w"), FiguraVec4.class, String.class);

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.reset"
    )
    public static void reset(FiguraVec4 vec) {
        vec.reset();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec4.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"vec", "x", "y", "z", "w"}
            ),
            description = "vector_n.set"
    )
    public static void set(FiguraVec4 vec, Double x, Double y, Double z, Double w) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        vec.set(x, y, z, w);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.copy"
    )
    public static FiguraVec4 copy(FiguraVec4 vec) {
        return vec.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalized"
    )
    public static void normalize(FiguraVec4 vec) {
        vec.normalize();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalized"
    )
    public static FiguraVec4 normalized(FiguraVec4 vec) {
        return vec.normalized();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec4.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamp_length"
    )
    public static void clampLength(@LuaNotNil FiguraVec4 arg, Double minLength, Double maxLength) {
        arg.clampLength(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec4.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamped"
    )
    public static FiguraVec4 clamped(@LuaNotNil FiguraVec4 arg, Double minLength, Double maxLength) {
        return arg.clamped(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length"
    )
    public static double length(@LuaNotNil FiguraVec4 arg) {
        return arg.length();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(@LuaNotNil FiguraVec4 arg) {
        return arg.lengthSquared();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec4.class, FiguraVec4.class},
                    argumentNames = {"vec1", "vec2"}
            ),
            description = "vector_n.dot"
    )
    public static double dot(@LuaNotNil FiguraVec4 arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg1.dot(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_rad"
    )
    public static FiguraVec4 toRad(@LuaNotNil FiguraVec4 vec) {
        return vec.toRad();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_deg"
    )
    public static FiguraVec4 toDeg(@LuaNotNil FiguraVec4 vec) {
        return vec.toDeg();
    }
}
