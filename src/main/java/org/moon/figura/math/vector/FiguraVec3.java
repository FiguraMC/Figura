package org.moon.figura.math.vector;

import net.minecraft.core.BlockPos;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.lua.types.LuaIPairsIterator;
import org.moon.figura.lua.types.LuaPairsIterator;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;
import org.terasology.jnlua.LuaRuntimeException;

import java.util.List;

@LuaWhitelist
@LuaTypeDoc(
        name = "Vector3",
        description = "vector3"
)
public class FiguraVec3 extends FiguraVector<FiguraVec3> implements CachedType {

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

    @Override
    public void reset() {
        x = y = z = 0;
    }

    @Override
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

    @Override
    public double lengthSquared() {
        return x*x+y*y+z*z;
    }

    @Override
    public FiguraVec3 copy() {
        FiguraVec3 result = of();
        result.set(this);
        return result;
    }

    @Override
    public double dot(FiguraVec3 o) {
        return x*o.x+y*o.y+z*o.z;
    }

    @Override
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

    @Override
    public void set(FiguraVec3 o) {
        set(o.x, o.y, o.z);
    }
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void add(FiguraVec3 o) {
        add(o.x, o.y, o.z);
    }
    public void add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    @Override
    public void subtract(FiguraVec3 o) {
        subtract(o.x, o.y, o.z);
    }
    public void subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    @Override
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

    @Override
    public void divide(FiguraVec3 o) {
        divide(o.x, o.y, o.z);
    }
    public void divide(double x, double y, double z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
    }

    @Override
    public void reduce(FiguraVec3 o) {
        reduce(o.x, o.y, o.z);
    } //modulo
    public void reduce(double x, double y, double z) {
        this.x = ((this.x % x) + x) % x;
        this.y = ((this.y % y) + y) % y;
        this.z = ((this.z % z) + z) % z;

        if (x < 0) this.x -= x;
        if (y < 0) this.y -= y;
        if (z < 0) this.z -= z;
    }

    @Override
    public void iDivide(FiguraVec3 o) {
        iDivide(o.x, o.y, o.z);
    }
    public void iDivide(double x, double y, double z) {
        this.x = Math.floor(this.x / x);
        this.y = Math.floor(this.y / y);
        this.z = Math.floor(this.z / z);
    }

    @Override
    public void scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
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
    public static FiguraVec3 __add(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg1.plus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __sub(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg1.minus(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mul(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg1.times(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mul(@LuaNotNil FiguraVec3 arg1, @LuaNotNil Double arg2) {
        return arg1.scaled(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mul(@LuaNotNil Double arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg2.scaled(arg1);
    }

    @LuaWhitelist
    public static FiguraVec3 __div(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.dividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __div(@LuaNotNil FiguraVec3 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.scaled(1 / arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mod(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        return arg1.mod(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __mod(@LuaNotNil FiguraVec3 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to reduce mod 0");
        FiguraVec3 modulus = FiguraVec3.of(arg2, arg2, arg2);
        FiguraVec3 result = arg1.mod(modulus);
        modulus.free();
        return result;
    }

    @LuaWhitelist
    public static FiguraVec3 __idiv(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        if (arg2.x == 0 || arg2.y == 0 || arg2.z == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        return arg1.iDividedBy(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __idiv(@LuaNotNil FiguraVec3 arg1, @LuaNotNil Double arg2) {
        if (arg2 == 0)
            throw new LuaRuntimeException("Attempt to divide by 0");
        FiguraVec3 divisor = FiguraVec3.of(arg2, arg2, arg2);
        FiguraVec3 result = arg1.iDividedBy(divisor);
        divisor.free();
        return result;
    }

    @LuaWhitelist
    public static boolean __eq(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg1.equals(arg2);
    }

    @LuaWhitelist
    public static FiguraVec3 __unm(@LuaNotNil FiguraVec3 arg1) {
        return arg1.scaled(-1);
    }

    @LuaWhitelist
    public static int __len(@LuaNotNil FiguraVec3 arg1) {
        return 3;
    }

    @LuaWhitelist
    public static String __tostring(@LuaNotNil FiguraVec3 arg1) {
        return arg1.toString();
    }

    //Fallback for fetching a key that isn't in the table
    @LuaWhitelist
    public static Object __index(@LuaNotNil FiguraVec3 arg1, @LuaNotNil String arg2) {
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
            throw new LuaRuntimeException("Invalid swizzle: " + arg2);
        double[] vals = new double[len];
        for (int i = 0; i < len; i++)
            vals[i] = switch (arg2.charAt(i)) {
                case '1', 'x', 'r' -> arg1.x;
                case '2', 'y', 'g' -> arg1.y;
                case '3', 'z', 'b' -> arg1.z;
                case '_' -> 0;
                default -> throw new LuaRuntimeException("Invalid swizzle: " + arg2);
            };
        return MathUtils.sizedVector(vals);
    }

    @LuaWhitelist
    public static LuaIPairsIterator<FiguraVec3> __ipairs(@LuaNotNil FiguraVec3 arg) {
        return iPairsIterator;
    }
    private static final LuaIPairsIterator<FiguraVec3> iPairsIterator = new LuaIPairsIterator<>(FiguraVec3.class);

    @LuaWhitelist
    public static LuaPairsIterator<FiguraVec3, String> __pairs(@LuaNotNil FiguraVec3 arg) {
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
                    argumentNames = "vec"
            ),
            description = "vector_n.reset"
    )
    public static void reset(FiguraVec3 vec) {
        vec.reset();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, Double.class, Double.class, Double.class},
                    argumentNames = {"vec", "x", "y", "z"}
            ),
            description = "vector_n.set"
    )
    public static void set(FiguraVec3 vec, Double x, Double y, Double z) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        vec.set(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.copy"
    )
    public static FiguraVec3 copy(FiguraVec3 vec) {
        return vec.copy();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalize"
    )
    public static void normalize(FiguraVec3 vec) {
        vec.normalize();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.normalized"
    )
    public static FiguraVec3 normalized(FiguraVec3 vec) {
        return vec.normalized();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamp_length"
    )
    public static void clampLength(@LuaNotNil FiguraVec3 arg, Double minLength, Double maxLength) {
        arg.clampLength(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, Double.class, Double.class},
                    argumentNames = {"vec", "minLength", "maxLength"}
            ),
            description = "vector_n.clamped"
    )
    public static FiguraVec3 clamped(@LuaNotNil FiguraVec3 arg, Double minLength, Double maxLength) {
        return arg.clamped(minLength, maxLength);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length"
    )
    public static double length(@LuaNotNil FiguraVec3 arg) {
        return arg.length();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.length_squared"
    )
    public static double lengthSquared(@LuaNotNil FiguraVec3 arg) {
        return arg.lengthSquared();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                    argumentNames = {"vec1", "vec2"}
            ),
            description = "vector_n.dot"
    )
    public static double dot(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg1.dot(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_rad"
    )
    public static FiguraVec3 toRad(@LuaNotNil FiguraVec3 vec) {
        return vec.toRad();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector_n.to_deg"
    )
    public static FiguraVec3 toDeg(@LuaNotNil FiguraVec3 vec) {
        return vec.toDeg();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraVec3.class, FiguraVec3.class},
                    argumentNames = {"vec1", "vec2"}
            ),
            description = "vector3.cross"
    )
    public static FiguraVec3 cross(@LuaNotNil FiguraVec3 arg1, @LuaNotNil FiguraVec3 arg2) {
        return arg1.crossed(arg2);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraVec3.class,
                    argumentNames = "vec"
            ),
            description = "vector3.augmented"
    )
    public static FiguraVec4 augmented(@LuaNotNil FiguraVec3 arg1) {
        return arg1.augmented();
    }
}
