package org.moon.figura.math.vector;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.MathUtils;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector4",
        description = "vector4"
)
public class FiguraVec4 implements CachedType {

    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = true, description = "vector_n.x"
    )
    public double x;
    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = true, description = "vector_n.y"
    )
    public double y;
    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = true, description = "vector_n.z"
    )
    public double z;
    @LuaWhitelist
    @LuaFieldDoc(
            canEdit = true, description = "vector_n.w"
    )
    public double w;

    private FiguraVec4() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraVec4> CACHE = CacheUtils.getCache(FiguraVec4::new);
    public void reset() {
        x = y = z = w = 0;
    }
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

    public double lengthSquared() {
        return x*x+y*y+z*z+w*w;
    }
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    public FiguraVec4 copy() {
        FiguraVec4 result = of();
        result.set(this);
        return result;
    }
    public double dot(FiguraVec4 o) {
        return x*o.x+y*o.y+z*o.z+w*o.w;
    }
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
        return "{" + x + ", " + y + ", " + z + ", " + w + "}";
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraVec4 o) {
        set(o.x, o.y, o.z, o.w);
    }
    public void set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void add(FiguraVec4 o) {
        add(o.x, o.y, o.z, o.w);
    }
    public void add(double x, double y, double z, double w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
    }

    public void subtract(FiguraVec4 o) {
        subtract(o.x, o.y, o.z, o.w);
    }
    public void subtract(double x, double y, double z, double w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
    }

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

    public void divide(FiguraVec4 o) {
        divide(o.x, o.y, o.z, o.w);
    }
    public void divide(double x, double y, double z, double w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
    }

    public void reduce(FiguraVec4 o) {
        reduce(o.x, o.y, o.z, o.w);
    } //modulo
    public void reduce(double x, double y, double z, double w) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
        this.w %= w;
    }

    public void iDivide(FiguraVec4 o) {
        iDivide(o.x, o.y, o.z, o.w);
    }
    public void iDivide(double x, double y, double z, double w) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
        this.z = Math.floor(this.z / z);
        this.w = Math.floor(this.w / w);
    }

    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        this.w *= factor;
    }
    public void normalize() {
        double l = length();
        if (l > 0)
            scale(1 / l);
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraVec4 plus(FiguraVec4 o) {
        return plus(o.x, o.y, o.z, o.w);
    }
    public FiguraVec4 plus(double x, double y, double z, double w) {
        FiguraVec4 result = copy();
        result.add(x, y, z, w);
        return result;
    }

    public FiguraVec4 minus(FiguraVec4 o) {
        return minus(o.x, o.y, o.z, o.w);
    }
    public FiguraVec4 minus(double x, double y, double z, double w) {
        FiguraVec4 result = copy();
        result.subtract(x, y, z, w);
        return result;
    }

    public FiguraVec4 times(FiguraVec4 o) {
        return times(o.x, o.y, o.z, o.w);
    }
    public FiguraVec4 times(double x, double y, double z, double w) {
        FiguraVec4 result = copy();
        result.multiply(x, y, z, w);
        return result;
    }
    public FiguraVec4 times(FiguraMat4 mat) {
        FiguraVec4 result = copy();
        result.multiply(mat);
        return result;
    }

    public FiguraVec4 dividedBy(FiguraVec4 o) {
        return dividedBy(o.x, o.y, o.z, o.w);
    }
    public FiguraVec4 dividedBy(double x, double y, double z, double w) {
        FiguraVec4 result = copy();
        result.divide(x, y, z, w);
        return result;
    }

    public FiguraVec4 mod(FiguraVec4 o) {
        return mod(o.x, o.y, o.z, o.w);
    }
    public FiguraVec4 mod(double x, double y, double z, double w) {
        FiguraVec4 result = copy();
        result.reduce(x, y, z, w);
        return result;
    }

    public FiguraVec4 iDividedBy(FiguraVec4 o) {
        return iDividedBy(o.x, o.y, o.z, o.w);
    }
    public FiguraVec4 iDividedBy(double x, double y, double z, double w) {
        FiguraVec4 result = copy();
        result.iDivide(x, y, z, w);
        return result;
    }

    public FiguraVec4 scaled(double factor) {
        FiguraVec4 result = copy();
        result.scale(factor);
        return result;
    }
    public FiguraVec4 normalized() {
        FiguraVec4 result = copy();
        result.normalize();
        return result;
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec4 __add(FiguraVec4 arg1, FiguraVec4 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __sub(FiguraVec4 arg1, FiguraVec4 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mul(FiguraVec4 arg1, FiguraVec4 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mul(FiguraVec4 arg1, Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mul(Double arg1, FiguraVec4 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec4 __div(FiguraVec4 arg1, FiguraVec4 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __div(FiguraVec4 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mod(FiguraVec4 arg1, FiguraVec4 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __mod(FiguraVec4 arg1, Double arg2) {
        if (arg2== 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2, arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __idiv(FiguraVec4 arg1, FiguraVec4 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0 || arg2.w == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __idiv(FiguraVec4 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2, arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static boolean __eq(FiguraVec4 arg1, FiguraVec4 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec4 __unm(FiguraVec4 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(FiguraVec4 arg1) {
        return 6;
    }

    @LuaWhitelist
    public static String __tostring(FiguraVec4 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(FiguraVec4 arg1, String arg2) {
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
            throw new IllegalArgumentException("Invalid swizzle: " + arg2);
        double[] vals = new double[len];
        for (int i = 0; i < len; i++)
            vals[i] = switch (arg2.charAt(i)) {
                case '1', 'x', 'r' -> arg1.x;
                case '2', 'y', 'g' -> arg1.y;
                case '3', 'z', 'b' -> arg1.z;
                case '4', 'w', 'a' -> arg1.w;
                case '_' -> 0;
                default -> throw new IllegalArgumentException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec4> __ipairs(FiguraVec4 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec4> iPairsIterator = new LuaIPairsIterator<>(FiguraVec4.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec4, String> __pairs(FiguraVec4 arg) {
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
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "vector_n.length"
    )
    public static double length(FiguraVec4 arg) {
        return Math.sqrt(lengthSquared(arg));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec4.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(FiguraVec4 arg) {
        return arg.dot(arg);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec4.class, FiguraVec4.class},
                    argumentNames = {"vec1", "vec2"},
                    returnType = Double.class
            ),
            description = "vector_n.dot"
    )
    public static double dot(FiguraVec4 arg1, FiguraVec4 arg2) {
        return arg1.dot(arg2);
    }
}
