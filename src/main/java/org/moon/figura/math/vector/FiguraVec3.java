package org.moon.figura.math.vector;

import net.minecraft.core.BlockPos;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector3",
        description = "vector3"
)
public class FiguraVec3 implements CachedType {

    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.x")
    public double x;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.y")
    public double y;
    @LuaWhitelist
    @LuaFieldDoc(description = "vector_n.z")
    public double z;

    private FiguraVec3() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraVec3> CACHE = CacheUtils.getCache(FiguraVec3::new);
    public void reset() {
        x = y = z = 0;
    }
    public void free() {
        CACHE.offerOld(this);
    }
    public static FiguraVec3 of() {
        return CACHE.getFresh();
    }
    public static FiguraVec3 of(double x, double y, double z) {
        FiguraVec3 result = of();
        result.set(x, y, z);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------

    public double lengthSquared() {
        return x*x+y*y+z*z;
    }
    public double length() {
        return Math.sqrt(lengthSquared());
    }
    public FiguraVec3 copy() {
        FiguraVec3 result = of();
        result.set(this);
        return result;
    }
    public double dot(FiguraVec3 o) {
        return x*o.x+y*o.y+z*o.z;
    }
    public boolean equals(FiguraVec3 o) {
        return x==o.x && y==o.y && z==o.z;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraVec3 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "{" + (float) x + ", " + (float) y + ", " + (float) z + "}";
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraVec3 o) {
        set(o.x, o.y, o.z);
    }
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(FiguraVec3 o) {
        add(o.x, o.y, o.z);
    }
    public void add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void subtract(FiguraVec3 o) {
        subtract(o.x, o.y, o.z);
    }
    public void subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    public void multiply(FiguraVec3 o) {
        multiply(o.x, o.y, o.z);
    }
    public void multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
    }
    public void multiply(FiguraMat3 mat) {
        set(
                mat.v11*x+mat.v12*y+mat.v13*z,
                mat.v21*x+mat.v22*y+mat.v23*z,
                mat.v31*x+mat.v32*y+mat.v33*z
        );
    }

    public void divide(FiguraVec3 o) {
        divide(o.x, o.y, o.z);
    }
    public void divide(double x, double y, double z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
    }

    public void reduce(FiguraVec3 o) {
        reduce(o.x, o.y, o.z);
    } //modulo
    public void reduce(double x, double y, double z) {
        this.x %= x;
        this.y %= y;
        this.z %= z;
    }

    public void iDivide(FiguraVec3 o) {
        iDivide(o.x, o.y, o.z);
    }
    public void iDivide(double x, double y, double z) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
        this.z = Math.floor(this.z / z);
    }

    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
    }
    public void normalize() {
        double l = length();
        if (l > 0)
            scale(1 / l);
    }

    public void cross(FiguraVec3 other) {
        double nx = y * other.z - z * other.y;
        double ny = z * other.x - x * other.z;
        double nz = x * other.y - y * other.x;
        set(nx, ny, nz);
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraVec3 plus(FiguraVec3 o) {
        return plus(o.x, o.y, o.z);
    }
    public FiguraVec3 plus(double x, double y, double z) {
        FiguraVec3 result = copy();
        result.add(x, y, z);
        return result;
    }

    public FiguraVec3 minus(FiguraVec3 o) {
        return minus(o.x, o.y, o.z);
    }
    public FiguraVec3 minus(double x, double y, double z) {
        FiguraVec3 result = copy();
        result.subtract(x, y, z);
        return result;
    }

    public FiguraVec3 times(FiguraVec3 o) {
        return times(o.x, o.y, o.z);
    }
    public FiguraVec3 times(double x, double y, double z) {
        FiguraVec3 result = copy();
        result.multiply(x, y, z);
        return result;
    }
    public FiguraVec3 times(FiguraMat3 mat) {
        FiguraVec3 result = copy();
        result.multiply(mat);
        return result;
    }

    public FiguraVec3 dividedBy(FiguraVec3 o) {
        return dividedBy(o.x, o.y, o.z);
    }
    public FiguraVec3 dividedBy(double x, double y, double z) {
        FiguraVec3 result = copy();
        result.divide(x, y, z);
        return result;
    }

    public FiguraVec3 mod(FiguraVec3 o) {
        return mod(o.x, o.y, o.z);
    }
    public FiguraVec3 mod(double x, double y, double z) {
        FiguraVec3 result = copy();
        result.reduce(x, y, z);
        return result;
    }

    public FiguraVec3 iDividedBy(FiguraVec3 o) {
        return iDividedBy(o.x, o.y, o.z);
    }
    public FiguraVec3 iDividedBy(double x, double y, double z) {
        FiguraVec3 result = copy();
        result.iDivide(x, y, z);
        return result;
    }

    public FiguraVec3 scaled(double factor) {
        FiguraVec3 result = copy();
        result.scale(factor);
        return result;
    }
    public FiguraVec3 normalized() {
        FiguraVec3 result = copy();
        result.normalize();
        return result;
    }

    public FiguraVec3 crossed(FiguraVec3 other) {
        double nx = y * other.z - z * other.y;
        double ny = z * other.x - x * other.z;
        double nz = x * other.y - y * other.x;
        return FiguraVec3.of(nx, ny, nz);
    }

    public FiguraVec4 augmented() {
        return FiguraVec4.of(x, y, z, 1);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(x, y, z);
    }

    //----------------------------------------------------------------

    // METAMETHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static FiguraVec3 __add(FiguraVec3 arg1, FiguraVec3 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __sub(FiguraVec3 arg1, FiguraVec3 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mul(FiguraVec3 arg1, FiguraVec3 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mul(FiguraVec3 arg1, Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mul(Double arg1, FiguraVec3 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec3 __div(FiguraVec3 arg1, FiguraVec3 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __div(FiguraVec3 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mod(FiguraVec3 arg1, FiguraVec3 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mod(FiguraVec3 arg1, Double arg2) {
        if (arg2== 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __idiv(FiguraVec3 arg1, FiguraVec3 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __idiv(FiguraVec3 arg1, Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2, arg2, arg2);
    }

    @LuaWhitelist
    public static boolean __eq(FiguraVec3 arg1, FiguraVec3 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __unm(FiguraVec3 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(FiguraVec3 arg1) {
        return 6;
    }

    @LuaWhitelist
    public static String __tostring(FiguraVec3 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(FiguraVec3 arg1, String arg2) {
        if (arg2 == null)
            return null;
        int len = arg2.length();
        if (len == 1) return switch(arg2) {
            case "1", "r" -> arg1.x;
            case "2", "g" -> arg1.y;
            case "3", "b" -> arg1.z;
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
                case '_' -> 0;
                default -> throw new IllegalArgumentException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec3> __ipairs(FiguraVec3 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec3> iPairsIterator = new LuaIPairsIterator<>(FiguraVec3.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec3, String> __pairs(FiguraVec3 arg) {
        return pairsIterator;
    }
    private static final LuaPairsIterator<FiguraVec3, String> pairsIterator =
            new LuaPairsIterator<>(List.of("x", "y", "z"), FiguraVec3.class, String.class);

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "vector_n.length"
    )
    public static double length(FiguraVec3 arg) {
        return Math.sqrt(lengthSquared(arg));
    }


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec",
                    returnType = Double.class
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(FiguraVec3 arg) {
        return arg.x*arg.x + arg.y*arg.y + arg.z*arg.z;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                    argumentNames = {"vec1", "vec2"},
                    returnType = Double.class
            ),
            description = "vector_n.dot"
    )
    public static double dot(FiguraVec3 arg1, FiguraVec3 arg2) {
        return arg1.dot(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                    argumentNames = {"vec1", "vec2"},
                    returnType = FiguraVec3.class
            ),
            description = "vector3.cross"
    )
    public static FiguraVec3 cross(FiguraVec3 arg1, FiguraVec3 arg2) {
        return arg1.crossed(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec",
                    returnType = FiguraVec4.class
            ),
            description = "vector3.augmented"
    )
    public static FiguraVec4 augmented(FiguraVec3 arg1) {
        return arg1.augmented();
    }
}
