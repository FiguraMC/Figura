package org.moon.figura.math.matrix;

import net.minecraft.util.math.Matrix3f;
import org.lwjgl.BufferUtils;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

import java.nio.FloatBuffer;

@LuaWhitelist
public class FiguraMat3 implements CachedType {

    //Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    @LuaWhitelist
    public double v11, v12, v13, v21, v22, v23, v31, v32, v33;

    private FiguraMat3 cachedInverse = null;
    private double cachedDeterminant = Double.MAX_VALUE;


    private FiguraMat3() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraMat3> CACHE = CacheUtils.getCache(FiguraMat3::new);
    public void reset() {
        v12=v13=v21=v23=v31=v32 = 0;
        v11=v22=v33 = 1;
        cachedInverse = null;
        cachedDeterminant = Double.MAX_VALUE;
    }
    public void free() {
        CACHE.offerOld(this);
    }
    public static FiguraMat3 of() {
        return CACHE.getFresh();
    }
    public static FiguraMat3 of(double n11, double n21, double n31,
                                double n12, double n22, double n32,
                                double n13, double n23, double n33) {
        FiguraMat3 result = of();
        result.set(n11, n21, n31, n12, n22, n32, n13, n23, n33);
        return result;
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------
    public double det() {
        if (cachedDeterminant != Double.MAX_VALUE)
            return cachedDeterminant;

        double sub11 = v22 * v33 - v23 * v32;
        double sub12 = v21 * v33 - v23 * v31;
        double sub13 = v21 * v32 - v22 * v31;

        cachedDeterminant = v11 * sub11 - v12 * sub12 + v13 * sub13;
        if (cachedDeterminant == 0) cachedDeterminant = Double.MIN_VALUE; //Prevent divide by 0 errors
        return cachedDeterminant;
    }
    public FiguraMat3 copy() {
        FiguraMat3 result = of();
        result.set(this);
        result.cachedInverse = cachedInverse;
        result.cachedDeterminant = cachedDeterminant;
        return result;
    }
    private void invalidate() {
        if (cachedInverse != null)
            cachedInverse.free();
        cachedInverse = null;
        cachedDeterminant = Double.MAX_VALUE;
    }
    public boolean equals(FiguraMat3 o) {
        return
                v11 == o.v11 && v12 == o.v12 && v13 == o.v13
                && v21 == o.v21 && v22 == o.v22 && v23 == o.v23
                && v31 == o.v31 && v32 == o.v32 && v33 == o.v33;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraMat3 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "\n[  " + v11 + ", " + v12 + ", " + v13 + ", " +
                "\n   " + v21 + ", " + v22 + ", " + v23 +
                "\n   " + v31 + ", " + v32 + ", " + v33 +
                "  ]";
    }
    public FiguraVec3 getCol1() {
        return FiguraVec3.of(v11, v21, v31);
    }
    public FiguraVec3 getCol2() {
        return FiguraVec3.of(v12, v22, v32);
    }
    public FiguraVec3 getCol3() {
        return FiguraVec3.of(v13, v23, v33);
    }
    public FiguraVec3 getRow1() {
        return FiguraVec3.of(v11, v12, v13);
    }
    public FiguraVec3 getRow2() {
        return FiguraVec3.of(v21, v22, v23);
    }
    public FiguraVec3 getRow3() {
        return FiguraVec3.of(v31, v32, v33);
    }
    private static final FloatBuffer copyingBuffer = BufferUtils.createFloatBuffer(3*3);
    public static FiguraMat3 fromMatrix3f(Matrix3f mat) {
        copyingBuffer.clear();
        mat.writeColumnMajor(copyingBuffer);
        FiguraMat3 result = of();
        result.v11 = copyingBuffer.get();
        result.v21 = copyingBuffer.get();
        result.v31 = copyingBuffer.get();
        result.v12 = copyingBuffer.get();
        result.v22 = copyingBuffer.get();
        result.v32 = copyingBuffer.get();
        result.v13 = copyingBuffer.get();
        result.v23 = copyingBuffer.get();
        result.v33 = copyingBuffer.get();
        return result;
    }
    public Matrix3f toMatrix3f() {
        copyingBuffer.clear();
        copyingBuffer
                .put((float) v11).put((float) v21).put((float) v31)
                .put((float) v12).put((float) v22).put((float) v32)
                .put((float) v13).put((float) v23).put((float) v33);
        Matrix3f result = new Matrix3f();
        result.readColumnMajor(copyingBuffer);
        return result;
    }

    //----------------------------------------------------------------

    // STATIC CREATOR METHODS
    //----------------------------------------------------------------
    public static FiguraMat3 createScaleMatrix(double x, double y, double z) {
        FiguraMat3 result = of();
        result.v11 = x;
        result.v22 = y;
        result.v33 = z;
        return result;
    }
    public static FiguraMat3 createXRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat3 result = of();
        result.v22 = result.v33 = c;
        result.v23 = -s;
        result.v32 = s;
        return result;
    }
    public static FiguraMat3 createYRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat3 result = of();
        result.v11 = result.v33 = c;
        result.v13 = s;
        result.v31 = -s;
        return result;
    }
    public static FiguraMat3 createZRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat3 result = of();
        result.v11 = result.v22 = c;
        result.v12 = -s;
        result.v21 = s;
        return result;
    }
    public static FiguraMat3 createZYXRotationMatrix(double x, double y, double z) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);

        double a = Math.cos(x);
        double b = Math.sin(x);
        double c = Math.cos(y);
        double d = Math.sin(y);
        double e = Math.cos(z);
        double f = Math.sin(z);

        FiguraMat3 result = of();
        result.v11 = c*e;
        result.v12 = b*d*e - a*f;
        result.v13 = a*d*e + b*f;
        result.v21 = c*f;
        result.v22 = b*d*f + a*e;
        result.v23 = a*d*f - b*e;
        result.v31 = -d;
        result.v32 = b*c;
        result.v33 = a*c;
        return result;
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraMat3 o) {
        set(o.v11, o.v21, o.v31, o.v12, o.v22, o.v32, o.v13, o.v23, o.v33);
    }
    public void set(double n11, double n21, double n31,
                    double n12, double n22, double n32,
                    double n13, double n23, double n33) {
        v11 = n11;
        v12 = n12;
        v13 = n13;
        v21 = n21;
        v22 = n22;
        v23 = n23;
        v31 = n31;
        v32 = n32;
        v33 = n33;
        invalidate();
    }

    public void add(FiguraMat3 o) {
        add(o.v11, o.v21, o.v31, o.v12, o.v22, o.v32, o.v13, o.v23, o.v33);
    }
    public void add(double n11, double n21, double n31,
                    double n12, double n22, double n32,
                    double n13, double n23, double n33) {
        v11 += n11;
        v12 += n12;
        v13 += n13;
        v21 += n21;
        v22 += n22;
        v23 += n23;
        v31 += n31;
        v32 += n32;
        v33 += n33;
        invalidate();
    }

    public void subtract(FiguraMat3 o) {
        subtract(o.v11, o.v21, o.v31, o.v12, o.v22, o.v32, o.v13, o.v23, o.v33);
    }
    public void subtract(double n11, double n21, double n31,
                    double n12, double n22, double n32,
                    double n13, double n23, double n33) {
        v11 -= n11;
        v12 -= n12;
        v13 -= n13;
        v21 -= n21;
        v22 -= n22;
        v23 -= n23;
        v31 -= n31;
        v32 -= n32;
        v33 -= n33;
        invalidate();
    }

    public void scale(double x, double y, double z) {
        v11 *= x;
        v12 *= x;
        v13 *= x;
        v21 *= y;
        v22 *= y;
        v23 *= y;
        v31 *= z;
        v32 *= z;
        v33 *= z;
        invalidate();
    }

    public void rotateX(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv21 = c*v21 - s*v31;
        double nv22 = c*v22 - s*v32;
        double nv23 = c*v23 - s*v33;

        v31 = s*v21 + c*v31;
        v32 = s*v22 + c*v32;
        v33 = s*v23 + c*v33;

        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        invalidate();
    }

    public void rotateY(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 + s*v31;
        double nv12 = c*v12 + s*v32;
        double nv13 = c*v13 + s*v33;

        v31 = c*v31 - s*v11;
        v32 = c*v32 - s*v12;
        v33 = c*v33 - s*v13;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        invalidate();
    }

    public void rotateZ(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 - s*v21;
        double nv12 = c*v12 - s*v22;
        double nv13 = c*v13 - s*v23;

        v21 = c*v21 + s*v11;
        v22 = c*v22 + s*v12;
        v23 = c*v23 + s*v13;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        invalidate();
    }

    //Rotates using ZYX matrix order, meaning the X axis, then Y, then Z.
    public void rotateZYX(double x, double y, double z) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);

        double a = Math.cos(x);
        double b = Math.sin(x);
        double c = Math.cos(y);
        double d = Math.sin(y);
        double e = Math.cos(z);
        double f = Math.sin(z);

        double bc = b*c;
        double ac = a*c;
        double ce = c*e;
        double cf = c*f;
        double p1 = (b*d*e - a*f);
        double p2 = (a*d*e + b*f);
        double p3 = (a*e + b*d*f);
        double p4 = (a*d*f - b*e);

        double nv11 = ce*v11 + p1*v21 + p2*v31;
        double nv21 = cf*v11 + p3*v21 + p4*v31;
        double nv31 = -d*v11 + bc*v21 + ac*v31;

        double nv12 = ce*v12 + p1*v22 + p2*v32;
        double nv22 = cf*v12 + p3*v22 + p4*v32;
        double nv32 = -d*v12 + bc*v22 + ac*v32;

        double nv13 = ce*v13 + p1*v23 + p2*v33;
        double nv23 = cf*v13 + p3*v23 + p4*v33;
        double nv33 = -d*v13 + bc*v23 + ac*v33;

        v11 = nv11;
        v21 = nv21;
        v31 = nv31;
        v12 = nv12;
        v22 = nv22;
        v32 = nv32;
        v13 = nv13;
        v23 = nv23;
        v33 = nv33;
        invalidate();
    }

    public void multiply(FiguraMat3 o) {
        double nv11 = o.v11*v11+o.v12*v21+o.v13*v31;
        double nv12 = o.v11*v12+o.v12*v22+o.v13*v32;
        double nv13 = o.v11*v13+o.v12*v23+o.v13*v33;

        double nv21 = o.v21*v11+o.v22*v21+o.v23*v31;
        double nv22 = o.v21*v12+o.v22*v22+o.v23*v32;
        double nv23 = o.v21*v13+o.v22*v23+o.v23*v33;

        double nv31 = o.v31*v11+o.v32*v21+o.v33*v31;
        double nv32 = o.v31*v12+o.v32*v22+o.v33*v32;
        double nv33 = o.v31*v13+o.v32*v23+o.v33*v33;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v31 = nv31;
        v32 = nv32;
        v33 = nv33;
        invalidate();
    }

    //o is on the right.
    public void rightMultiply(FiguraMat3 o) {
        double nv11 = v11*o.v11+v12*o.v21+v13*o.v31;
        double nv12 = v11*o.v12+v12*o.v22+v13*o.v32;
        double nv13 = v11*o.v13+v12*o.v23+v13*o.v33;

        double nv21 = v21*o.v11+v22*o.v21+v23*o.v31;
        double nv22 = v21*o.v12+v22*o.v22+v23*o.v32;
        double nv23 = v21*o.v13+v22*o.v23+v23*o.v33;

        double nv31 = v31*o.v11+v32*o.v21+v33*o.v31;
        double nv32 = v31*o.v12+v32*o.v22+v33*o.v32;
        double nv33 = v31*o.v13+v32*o.v23+v33*o.v33;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v31 = nv31;
        v32 = nv32;
        v33 = nv33;
        invalidate();
    }

    public void transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        temp = v13; v13 = v31; v31 = temp;
        temp = v23; v23 = v32; v32 = temp;
        cachedInverse = null; //transposing doesn't invalidate the determinant
    }

    public void invert() {
        FiguraMat3 capture = copy();
        if (cachedInverse != null) {
            set(cachedInverse);
            cachedDeterminant = 1 / cachedDeterminant;
        } else {

            double sub11 = v22 * v33 - v23 * v32;
            double sub12 = v21 * v33 - v23 * v31;
            double sub13 = v21 * v32 - v22 * v31;
            double sub21 = v12 * v33 - v13 * v32;
            double sub22 = v11 * v33 - v13 * v31;
            double sub23 = v11 * v32 - v12 * v31;
            double sub31 = v12 * v23 - v13 * v22;
            double sub32 = v11 * v23 - v13 * v21;
            double sub33 = v11 * v22 - v12 * v21;

            double det = v11 * sub11 - v12 * sub12 + v13 * sub13;
            if (det == 0) det = Double.MIN_VALUE;
            det = 1/det;
            cachedDeterminant = det;
            set(
                    det * sub11,
                    -det * sub12,
                    det * sub13,
                    -det * sub21,
                    det * sub22,
                    -det * sub23,
                    det * sub31,
                    -det * sub32,
                    det * sub33
            );
        }
        cachedInverse = capture;
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraMat3 transposed() {
        FiguraMat3 result = copy();
        result.transpose();
        return result;
    }

    public FiguraMat3 inverted() {
        FiguraMat3 result = copy();
        result.invert();
        return result;
    }

    public FiguraMat3 plus(FiguraMat3 o) {
        FiguraMat3 result = copy();
        result.add(o);
        return result;
    }
    public FiguraMat3 plus(double n11, double n21, double n31,
                           double n12, double n22, double n32,
                           double n13, double n23, double n33) {
        FiguraMat3 result = copy();
        result.add(n11, n21, n31, n12, n22, n32, n13, n23, n33);
        return result;
    }

    public FiguraMat3 minus(FiguraMat3 o) {
        FiguraMat3 result = copy();
        result.subtract(o);
        return result;
    }
    public FiguraMat3 minus(double n11, double n21, double n31,
                            double n12, double n22, double n32,
                            double n13, double n23, double n33) {
        FiguraMat3 result = copy();
        result.subtract(n11, n21, n31, n12, n22, n32, n13, n23, n33);
        return result;
    }

    //Returns the product of the matrices, with "o" on the left.
    public FiguraMat3 times(FiguraMat3 o) {
        FiguraMat3 result = of();

        result.v11 = o.v11*v11+o.v12*v21+o.v13*v31;
        result.v12 = o.v11*v12+o.v12*v22+o.v13*v32;
        result.v13 = o.v11*v13+o.v12*v23+o.v13*v33;

        result.v21 = o.v21*v11+o.v22*v21+o.v23*v31;
        result.v22 = o.v21*v12+o.v22*v22+o.v23*v32;
        result.v23 = o.v21*v13+o.v22*v23+o.v23*v33;

        result.v31 = o.v31*v11+o.v32*v21+o.v33*v31;
        result.v32 = o.v31*v12+o.v32*v22+o.v33*v32;
        result.v33 = o.v31*v13+o.v32*v23+o.v33*v33;

        return result;
    }

    public FiguraVec3 times(FiguraVec3 vec) {
        FiguraVec3 result = FiguraVec3.of();
        result.x = v11*vec.x+v12*vec.y+v13*vec.z;
        result.y = v21*vec.x+v22*vec.y+v23*vec.z;
        result.z = v31*vec.x+v32*vec.y+v33*vec.z;
        return result;
    }

    // METAMETHODS
    //----------------------------------------------------------------
    @LuaWhitelist
    public static FiguraMat3 __add(FiguraMat3 arg1, FiguraMat3 arg2) {
        return arg1.plus(arg2);
    }
    @LuaWhitelist
    public static FiguraMat3 __sub(FiguraMat3 arg1, FiguraMat3 arg2) {
        return arg1.minus(arg2);
    }
    @LuaWhitelist
    public static FiguraMat3 __mul(FiguraMat3 arg1, FiguraMat3 arg2) {
        return arg2.times(arg1);
    }
    @LuaWhitelist
    public static FiguraVec3 __mul(FiguraMat3 arg1, FiguraVec3 arg2) {
        return arg1.times(arg2);
    }
    @LuaWhitelist
    public static boolean __eq(FiguraMat3 arg1, FiguraMat3 arg2) {
        return arg1.equals(arg2);
    }
    @LuaWhitelist
    public static int __len(FiguraMat3 arg1) {
        return 4;
    }
    @LuaWhitelist
    public static String __tostring(FiguraMat3 arg1) {
        return arg1.toString();
    }
    @LuaWhitelist
    public static Object __index(FiguraMat3 arg1, String arg2) {
        return switch (arg2) {
            case "1" -> arg1.getCol1();
            case "2" -> arg1.getCol2();
            case "3" -> arg1.getCol3();
            default -> null;
        };
    }

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    public static double det(FiguraMat3 mat) {
        return mat.det();
    }
    @LuaWhitelist
    public static void invert(FiguraMat3 mat) {
        mat.invert();
    }
    @LuaWhitelist
    public static FiguraMat3 getInverse(FiguraMat3 mat) {
        return mat.inverted();
    }
    @LuaWhitelist
    public static void transpose(FiguraMat3 mat) {
        mat.transpose();
    }
    @LuaWhitelist
    public static FiguraMat3 getTranspose(FiguraMat3 mat) {
        return mat.transposed();
    }

    @LuaWhitelist
    public static void rotate(FiguraMat3 mat, Object arg1, Double y, Double z) {
        if (arg1 instanceof Double x) {
            if (y != null && z != null)
                mat.rotateZYX(x, y, z);
            else
                throw new IllegalArgumentException("Cannot rotate using nil!");
        } else if (arg1 instanceof FiguraVec3 vec) {
            mat.rotateZYX(vec.x, vec.y, vec.z);
        } else {
            throw new IllegalArgumentException("Cannot rotate with argument " + arg1 + ".");
        }
    }

    @LuaWhitelist
    public static void scale(FiguraMat3 mat, Object arg1, Double y, Double z) {
        if (arg1 instanceof Double x) {
            if (y != null && z != null)
                mat.scale(x, y, z);
            else
                throw new IllegalArgumentException("Cannot scale using nil!");
        } else if (arg1 instanceof FiguraVec3 vec) {
            mat.scale(vec.x, vec.y, vec.z);
        } else {
            throw new IllegalArgumentException("Cannot scale with argument " + arg1 + ".");
        }
    }

    @LuaWhitelist
    public static FiguraVec3 getColumn(FiguraMat3 mat, Integer column) {
        if (column == null) throw new IllegalArgumentException("Cannot access nil column!");
        if (column <= 0 || column > 3) throw new IllegalArgumentException("Column " + column + " does not exist in a 3x3 matrix!");
        return switch (column) {
            case 1 -> mat.getCol1();
            case 2 -> mat.getCol2();
            case 3 -> mat.getCol3();
            default -> null;
        };
    }
    @LuaWhitelist
    public static FiguraVec3 getRow(FiguraMat3 mat, Integer row) {
        if (row == null) throw new IllegalArgumentException("Cannot access nil row!");
        if (row <= 0 || row > 3) throw new IllegalArgumentException("Row " + row + " does not exist in a 3x3 matrix!");
        return switch (row) {
            case 1 -> mat.getRow1();
            case 2 -> mat.getRow2();
            case 3 -> mat.getRow3();
            default -> null;
        };
    }

}
