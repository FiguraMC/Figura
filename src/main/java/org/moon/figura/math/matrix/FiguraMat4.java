package org.moon.figura.math.matrix;

import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.caching.CacheStack;
import org.moon.figura.utils.caching.CacheUtils;
import org.moon.figura.utils.caching.CachedType;

import java.nio.FloatBuffer;

@LuaWhitelist
@LuaTypeDoc(
        name = "Matrix4",
        description = "matrix4"
)
public class FiguraMat4 implements CachedType {

    //Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    @LuaWhitelist
    public double v11, v12, v13, v14, v21, v22, v23, v24, v31, v32, v33, v34, v41, v42, v43, v44;

    @LuaFieldDoc(description = "matrix_n.vrc")
    public double vRC;

    private FiguraMat4 cachedInverse = null;
    private double cachedDeterminant = Double.MAX_VALUE;


    private FiguraMat4() {}

    // CACHING METHODS
    //----------------------------------------------------------------
    private static final CacheUtils.Cache<FiguraMat4> CACHE = CacheUtils.getCache(FiguraMat4::new);
    public void reset() {
        v12=v13=v14=v21=v23=v24=v31=v32=v34=v41=v42=v43 = 0;
        v11=v22=v33=v44 = 1;
        cachedInverse = null;
        cachedDeterminant = Double.MAX_VALUE;
    }
    public void free() {
        CACHE.offerOld(this);
    }
    public static FiguraMat4 of() {
        return CACHE.getFresh();
    }
    public static FiguraMat4 of(double n11, double n21, double n31, double n41,
                                    double n12, double n22, double n32, double n42,
                                    double n13, double n23, double n33, double n43,
                                    double n14, double n24, double n34, double n44) {
        FiguraMat4 result = of();
        result.set(n11, n21, n31, n41, n12, n22, n32, n42, n13, n23, n33, n43, n14, n24, n34, n44);
        return result;
    }
    public static class Stack extends CacheStack<FiguraMat4, FiguraMat4> {
        public Stack() {
            this(CACHE);
        }
        public Stack(CacheUtils.Cache<FiguraMat4> cache) {
            super(cache);
        }
        @Override
        protected void modify(FiguraMat4 valueToModify, FiguraMat4 modifierArg) {
            valueToModify.rightMultiply(modifierArg);
        }
        @Override
        protected void copy(FiguraMat4 from, FiguraMat4 to) {
            to.set(from);
        }
    }

    //----------------------------------------------------------------

    // UTILITY METHODS
    //----------------------------------------------------------------
    public double det() {
        if (cachedDeterminant != Double.MAX_VALUE)
            return cachedDeterminant;
        //https://stackoverflow.com/a/44446912
        var A2323 = v33 * v44 - v34 * v43 ;
        var A1323 = v32 * v44 - v34 * v42 ;
        var A1223 = v32 * v43 - v33 * v42 ;
        var A0323 = v31 * v44 - v34 * v41 ;
        var A0223 = v31 * v43 - v33 * v41 ;
        var A0123 = v31 * v42 - v32 * v41 ;

        cachedDeterminant = v11 * ( v22 * A2323 - v23 * A1323 + v24 * A1223 )
                - v12 * ( v21 * A2323 - v23 * A0323 + v24 * A0223 )
                + v13 * ( v21 * A1323 - v22 * A0323 + v24 * A0123 )
                - v14 * ( v21 * A1223 - v22 * A0223 + v23 * A0123 ) ;
        if (cachedDeterminant == 0) cachedDeterminant = Double.MIN_VALUE; //Prevent divide by 0 errors
        return cachedDeterminant;
    }
    public FiguraMat4 copy() {
        FiguraMat4 result = of();
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
    public boolean equals(FiguraMat4 o) {
        return
                v11 == o.v11 && v12 == o.v12 && v13 == o.v13 && v14 == o.v14
                && v21 == o.v21 && v22 == o.v22 && v23 == o.v23 && v24 == o.v24
                && v31 == o.v31 && v32 == o.v32 && v33 == o.v33 && v34 == o.v34
                && v41 == o.v41 && v42 == o.v42 && v43 == o.v43 && v44 == o.v44;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraMat4 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "\n[  " +
                (float) v11 + ", " + (float) v12 + ", " + (float) v13 + ", " + (float) v14 +
                "\n   " + (float) v21 + ", " + v22 + ", " + (float) v23 + ", " + (float) v24 +
                "\n   " + (float) v31 + ", " + (float) v32 + ", " + (float) v33 + ", " + (float) v34 +
                "\n   " + (float) v41 + ", " + (float) v42 + ", " + (float) v43 + ", " + (float) v44 +
                "  ]";
    }
    public FiguraVec4 getCol1() {
        return FiguraVec4.of(v11, v21, v31, v41);
    }
    public FiguraVec4 getCol2() {
        return FiguraVec4.of(v12, v22, v32, v42);
    }
    public FiguraVec4 getCol3() {
        return FiguraVec4.of(v13, v23, v33, v43);
    }
    public FiguraVec4 getCol4() {
        return FiguraVec4.of(v14, v24, v34, v44);
    }
    public FiguraVec4 getRow1() {
        return FiguraVec4.of(v11, v12, v13, v14);
    }
    public FiguraVec4 getRow2() {
        return FiguraVec4.of(v21, v22, v23, v24);
    }
    public FiguraVec4 getRow3() {
        return FiguraVec4.of(v31, v32, v33, v34);
    }
    public FiguraVec4 getRow4() {
        return FiguraVec4.of(v41, v42, v43, v44);
    }
    private static final FloatBuffer copyingBuffer = BufferUtils.createFloatBuffer(4*4);
    public static FiguraMat4 fromMatrix4f(Matrix4f mat) {
        copyingBuffer.clear();
        mat.store(copyingBuffer);
        FiguraMat4 result = of();
        result.v11 = copyingBuffer.get();
        result.v21 = copyingBuffer.get();
        result.v31 = copyingBuffer.get();
        result.v41 = copyingBuffer.get();
        result.v12 = copyingBuffer.get();
        result.v22 = copyingBuffer.get();
        result.v32 = copyingBuffer.get();
        result.v42 = copyingBuffer.get();
        result.v13 = copyingBuffer.get();
        result.v23 = copyingBuffer.get();
        result.v33 = copyingBuffer.get();
        result.v43 = copyingBuffer.get();
        result.v14 = copyingBuffer.get();
        result.v24 = copyingBuffer.get();
        result.v34 = copyingBuffer.get();
        result.v44 = copyingBuffer.get();
        return result;
    }
    public Matrix4f toMatrix4f() {
        copyingBuffer.clear();
        copyingBuffer
                .put((float) v11).put((float) v21).put((float) v31).put((float) v41)
                .put((float) v12).put((float) v22).put((float) v32).put((float) v42)
                .put((float) v13).put((float) v23).put((float) v33).put((float) v43)
                .put((float) v14).put((float) v24).put((float) v34).put((float) v44);
        Matrix4f result = new Matrix4f();
        result.load(copyingBuffer);
        return result;
    }

    //----------------------------------------------------------------

    // STATIC CREATOR METHODS
    //----------------------------------------------------------------
    public static FiguraMat4 createScaleMatrix(double x, double y, double z) {
        FiguraMat4 result = of();
        result.v11 = x;
        result.v22 = y;
        result.v33 = z;
        return result;
    }
    public static FiguraMat4 createXRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat4 result = of();
        result.v22 = result.v33 = c;
        result.v23 = -s;
        result.v32 = s;
        return result;
    }
    public static FiguraMat4 createYRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat4 result = of();
        result.v11 = result.v33 = c;
        result.v13 = s;
        result.v31 = -s;
        return result;
    }
    public static FiguraMat4 createZRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat4 result = of();
        result.v11 = result.v22 = c;
        result.v12 = -s;
        result.v21 = s;
        return result;
    }
    public static FiguraMat4 createZYXRotationMatrix(double x, double y, double z) {
        x = Math.toRadians(x);
        y = Math.toRadians(y);
        z = Math.toRadians(z);

        double a = Math.cos(x);
        double b = Math.sin(x);
        double c = Math.cos(y);
        double d = Math.sin(y);
        double e = Math.cos(z);
        double f = Math.sin(z);

        FiguraMat4 result = of();
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
    public static FiguraMat4 createTranslationMatrix(double x, double y, double z) {
        FiguraMat4 result = of();
        result.v14 = x;
        result.v24 = y;
        result.v34 = z;
        return result;
    }
    public static FiguraMat4 createTranslationMatrix(FiguraVec3 amount) {
        return createTranslationMatrix(amount.x, amount.y, amount.z);
    }
    public static FiguraMat4 createTranslationMatrix(Vec3 amount) {
        return createTranslationMatrix(amount.x, amount.y, amount.z);
    }

    //----------------------------------------------------------------

    // MUTATOR METHODS
    //----------------------------------------------------------------

    public void set(FiguraMat4 o) {
        set(o.v11, o.v21, o.v31, o.v41, o.v12, o.v22, o.v32, o.v42, o.v13, o.v23, o.v33, o.v43, o.v14, o.v24, o.v34, o.v44);
    }
    public void set(double n11, double n21, double n31, double n41,
                    double n12, double n22, double n32, double n42,
                    double n13, double n23, double n33, double n43,
                    double n14, double n24, double n34, double n44) {
        v11 = n11;
        v12 = n12;
        v13 = n13;
        v14 = n14;
        v21 = n21;
        v22 = n22;
        v23 = n23;
        v24 = n24;
        v31 = n31;
        v32 = n32;
        v33 = n33;
        v34 = n34;
        v41 = n41;
        v42 = n42;
        v43 = n43;
        v44 = n44;
        invalidate();
    }

    public void add(FiguraMat4 o) {
        add(o.v11, o.v21, o.v31, o.v41, o.v12, o.v22, o.v32, o.v42, o.v13, o.v23, o.v33, o.v43, o.v14, o.v24, o.v34, o.v44);
    }
    public void add(double n11, double n21, double n31, double n41,
                    double n12, double n22, double n32, double n42,
                    double n13, double n23, double n33, double n43,
                    double n14, double n24, double n34, double n44) {
        v11 += n11;
        v12 += n12;
        v13 += n13;
        v14 += n14;
        v21 += n21;
        v22 += n22;
        v23 += n23;
        v24 += n24;
        v31 += n31;
        v32 += n32;
        v33 += n33;
        v34 += n34;
        v41 += n41;
        v42 += n42;
        v43 += n43;
        v44 += n44;
        invalidate();
    }

    public void subtract(FiguraMat4 o) {
        subtract(o.v11, o.v21, o.v31, o.v41, o.v12, o.v22, o.v32, o.v42, o.v13, o.v23, o.v33, o.v43, o.v14, o.v24, o.v34, o.v44);
    }
    public void subtract(double n11, double n21, double n31, double n41,
                    double n12, double n22, double n32, double n42,
                    double n13, double n23, double n33, double n43,
                    double n14, double n24, double n34, double n44) {
        v11 -= n11;
        v12 -= n12;
        v13 -= n13;
        v14 -= n14;
        v21 -= n21;
        v22 -= n22;
        v23 -= n23;
        v24 -= n24;
        v31 -= n31;
        v32 -= n32;
        v33 -= n33;
        v34 -= n34;
        v41 -= n41;
        v42 -= n42;
        v43 -= n43;
        v44 -= n44;
        invalidate();
    }

    public void translate(double x, double y, double z) {
        v11 += x * v41;
        v12 += x * v42;
        v13 += x * v43;
        v14 += x * v44;

        v21 += y * v41;
        v22 += y * v42;
        v23 += y * v43;
        v24 += y * v44;

        v31 += z * v41;
        v32 += z * v42;
        v33 += z * v43;
        v34 += z * v44;
        invalidate();
    }
    public void translate(FiguraVec3 amount) {
        translate(amount.x, amount.y, amount.z);
    }
    public void translate(Vec3 amount) {
        translate(amount.x, amount.y, amount.z);
    }

    public void scale(double x, double y, double z) {
        v11 *= x;
        v12 *= x;
        v13 *= x;
        v14 *= x;
        v21 *= y;
        v22 *= y;
        v23 *= y;
        v24 *= y;
        v31 *= z;
        v32 *= z;
        v33 *= z;
        v34 *= z;
        invalidate();
    }

    public void rotateX(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv21 = c*v21 - s*v31;
        double nv22 = c*v22 - s*v32;
        double nv23 = c*v23 - s*v33;
        double nv24 = c*v24 - s*v34;

        v31 = s*v21 + c*v31;
        v32 = s*v22 + c*v32;
        v33 = s*v23 + c*v33;
        v34 = s*v24 + c*v34;

        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v24 = nv24;
        invalidate();
    }

    public void rotateY(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 + s*v31;
        double nv12 = c*v12 + s*v32;
        double nv13 = c*v13 + s*v33;
        double nv14 = c*v14 + s*v34;

        v31 = c*v31 - s*v11;
        v32 = c*v32 - s*v12;
        v33 = c*v33 - s*v13;
        v34 = c*v34 - s*v14;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v14 = nv14;
        invalidate();
    }

    public void rotateZ(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c*v11 - s*v21;
        double nv12 = c*v12 - s*v22;
        double nv13 = c*v13 - s*v23;
        double nv14 = c*v14 - s*v24;

        v21 = c*v21 + s*v11;
        v22 = c*v22 + s*v12;
        v23 = c*v23 + s*v13;
        v24 = c*v24 + s*v14;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v14 = nv14;
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

        double nv14 = ce*v14 + p1*v24 + p2*v34;
        double nv24 = cf*v14 + p3*v24 + p4*v34;
        v34 = -d*v14 + bc*v24 + ac*v34;

        v11 = nv11;
        v21 = nv21;
        v31 = nv31;
        v12 = nv12;
        v22 = nv22;
        v32 = nv32;
        v13 = nv13;
        v23 = nv23;
        v33 = nv33;
        v14 = nv14;
        v24 = nv24;
        invalidate();
    }

    public void multiply(FiguraMat4 o) {
        double nv11 = o.v11*v11+o.v12*v21+o.v13*v31+o.v14*v41;
        double nv12 = o.v11*v12+o.v12*v22+o.v13*v32+o.v14*v42;
        double nv13 = o.v11*v13+o.v12*v23+o.v13*v33+o.v14*v43;
        double nv14 = o.v11*v14+o.v12*v24+o.v13*v34+o.v14*v44;

        double nv21 = o.v21*v11+o.v22*v21+o.v23*v31+o.v24*v41;
        double nv22 = o.v21*v12+o.v22*v22+o.v23*v32+o.v24*v42;
        double nv23 = o.v21*v13+o.v22*v23+o.v23*v33+o.v24*v43;
        double nv24 = o.v21*v14+o.v22*v24+o.v23*v34+o.v24*v44;

        double nv31 = o.v31*v11+o.v32*v21+o.v33*v31+o.v34*v41;
        double nv32 = o.v31*v12+o.v32*v22+o.v33*v32+o.v34*v42;
        double nv33 = o.v31*v13+o.v32*v23+o.v33*v33+o.v34*v43;
        double nv34 = o.v31*v14+o.v32*v24+o.v33*v34+o.v34*v44;

        double nv41 = o.v41*v11+o.v42*v21+o.v43*v31+o.v44*v41;
        double nv42 = o.v41*v12+o.v42*v22+o.v43*v32+o.v44*v42;
        double nv43 = o.v41*v13+o.v42*v23+o.v43*v33+o.v44*v43;
        v44 = o.v41*v14+o.v42*v24+o.v43*v34+o.v44*v44;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v14 = nv14;
        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v24 = nv24;
        v31 = nv31;
        v32 = nv32;
        v33 = nv33;
        v34 = nv34;
        v41 = nv41;
        v42 = nv42;
        v43 = nv43;
        invalidate();
    }

    //o is on the right.
    public void rightMultiply(FiguraMat4 o) {
        double nv11 = v11*o.v11+v12*o.v21+v13*o.v31+v14*o.v41;
        double nv12 = v11*o.v12+v12*o.v22+v13*o.v32+v14*o.v42;
        double nv13 = v11*o.v13+v12*o.v23+v13*o.v33+v14*o.v43;
        double nv14 = v11*o.v14+v12*o.v24+v13*o.v34+v14*o.v44;

        double nv21 = v21*o.v11+v22*o.v21+v23*o.v31+v24*o.v41;
        double nv22 = v21*o.v12+v22*o.v22+v23*o.v32+v24*o.v42;
        double nv23 = v21*o.v13+v22*o.v23+v23*o.v33+v24*o.v43;
        double nv24 = v21*o.v14+v22*o.v24+v23*o.v34+v24*o.v44;

        double nv31 = v31*o.v11+v32*o.v21+v33*o.v31+v34*o.v41;
        double nv32 = v31*o.v12+v32*o.v22+v33*o.v32+v34*o.v42;
        double nv33 = v31*o.v13+v32*o.v23+v33*o.v33+v34*o.v43;
        double nv34 = v31*o.v14+v32*o.v24+v33*o.v34+v34*o.v44;

        double nv41 = v41*o.v11+v42*o.v21+v43*o.v31+v44*o.v41;
        double nv42 = v41*o.v12+v42*o.v22+v43*o.v32+v44*o.v42;
        double nv43 = v41*o.v13+v42*o.v23+v43*o.v33+v44*o.v43;
        v44 = v41*o.v14+v42*o.v24+v43*o.v34+v44*o.v44;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v14 = nv14;
        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v24 = nv24;
        v31 = nv31;
        v32 = nv32;
        v33 = nv33;
        v34 = nv34;
        v41 = nv41;
        v42 = nv42;
        v43 = nv43;
        invalidate();
    }

    public void transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        temp = v13; v13 = v31; v31 = temp;
        temp = v14; v14 = v41; v41 = temp;
        temp = v23; v23 = v32; v32 = temp;
        temp = v24; v24 = v42; v42 = temp;
        temp = v34; v34 = v43; v43 = temp;
        cachedInverse = null; //transposing doesn't invalidate the determinant
    }

    public void invert() {
        FiguraMat4 capture = copy();
        if (cachedInverse != null) {
            set(cachedInverse);
            cachedDeterminant = 1 / cachedDeterminant;
        } else {
            //https://stackoverflow.com/a/44446912

            var A2323 = v33 * v44 - v34 * v43 ;
            var A1323 = v32 * v44 - v34 * v42 ;
            var A1223 = v32 * v43 - v33 * v42 ;
            var A0323 = v31 * v44 - v34 * v41 ;
            var A0223 = v31 * v43 - v33 * v41 ;
            var A0123 = v31 * v42 - v32 * v41 ;
            var A2313 = v23 * v44 - v24 * v43 ;
            var A1313 = v22 * v44 - v24 * v42 ;
            var A1213 = v22 * v43 - v23 * v42 ;
            var A2312 = v23 * v34 - v24 * v33 ;
            var A1312 = v22 * v34 - v24 * v32 ;
            var A1212 = v22 * v33 - v23 * v32 ;
            var A0313 = v21 * v44 - v24 * v41 ;
            var A0213 = v21 * v43 - v23 * v41 ;
            var A0312 = v21 * v34 - v24 * v31 ;
            var A0212 = v21 * v33 - v23 * v31 ;
            var A0113 = v21 * v42 - v22 * v41 ;
            var A0112 = v21 * v32 - v22 * v31 ;

            double det = v11 * ( v22 * A2323 - v23 * A1323 + v24 * A1223 )
                    - v12 * ( v21 * A2323 - v23 * A0323 + v24 * A0223 )
                    + v13 * ( v21 * A1323 - v22 * A0323 + v24 * A0123 )
                    - v14 * ( v21 * A1223 - v22 * A0223 + v23 * A0123 ) ;
            if (det == 0)
                det = Double.MIN_VALUE; //Prevent divide by 0 errors

            det = 1 / det;
            cachedDeterminant = det;

            set(
                det *   ( v22 * A2323 - v23 * A1323 + v24 * A1223 ),
                det * - ( v12 * A2323 - v13 * A1323 + v14 * A1223 ),
                det *   ( v12 * A2313 - v13 * A1313 + v14 * A1213 ),
                det * - ( v12 * A2312 - v13 * A1312 + v14 * A1212 ),
                det * - ( v21 * A2323 - v23 * A0323 + v24 * A0223 ),
                det *   ( v11 * A2323 - v13 * A0323 + v14 * A0223 ),
                det * - ( v11 * A2313 - v13 * A0313 + v14 * A0213 ),
                det *   ( v11 * A2312 - v13 * A0312 + v14 * A0212 ),
                det *   ( v21 * A1323 - v22 * A0323 + v24 * A0123 ),
                det * - ( v11 * A1323 - v12 * A0323 + v14 * A0123 ),
                det *   ( v11 * A1313 - v12 * A0313 + v14 * A0113 ),
                det * - ( v11 * A1312 - v12 * A0312 + v14 * A0112 ),
                det * - ( v21 * A1223 - v22 * A0223 + v23 * A0123 ),
                det *   ( v11 * A1223 - v12 * A0223 + v13 * A0123 ),
                det * - ( v11 * A1213 - v12 * A0213 + v13 * A0113 ),
                det *   ( v11 * A1212 - v12 * A0212 + v13 * A0112 )
            );
            transpose();
        }
        cachedInverse = capture;
    }

    //----------------------------------------------------------------

    // GENERATOR METHODS
    //----------------------------------------------------------------

    public FiguraMat4 transposed() {
        FiguraMat4 result = copy();
        result.transpose();
        return result;
    }

    public FiguraMat4 inverted() {
        FiguraMat4 result = copy();
        result.invert();
        return result;
    }

    public FiguraMat3 deaugmented() {
        FiguraMat3 result = FiguraMat3.of();
        result.set(v11, v21, v31, v12, v22, v32, v13, v23, v33);
        return result;
    }

    public FiguraMat4 plus(FiguraMat4 o) {
        FiguraMat4 result = copy();
        result.add(o);
        return result;
    }
    public FiguraMat4 plus(double n11, double n21, double n31, double n41,
                           double n12, double n22, double n32, double n42,
                           double n13, double n23, double n33, double n43,
                           double n14, double n24, double n34, double n44) {
        FiguraMat4 result = copy();
        result.add(n11, n21, n31, n41, n12, n22, n32, n42, n13, n23, n33, n43, n14, n24, n34, n44);
        return result;
    }

    public FiguraMat4 minus(FiguraMat4 o) {
        FiguraMat4 result = copy();
        result.subtract(o);
        return result;
    }
    public FiguraMat4 minus(double n11, double n21, double n31, double n41,
                           double n12, double n22, double n32, double n42,
                           double n13, double n23, double n33, double n43,
                           double n14, double n24, double n34, double n44) {
        FiguraMat4 result = copy();
        result.subtract(n11, n21, n31, n41, n12, n22, n32, n42, n13, n23, n33, n43, n14, n24, n34, n44);
        return result;
    }

    //Returns the product of the matrices, with "o" on the left.
    public FiguraMat4 times(FiguraMat4 o) {
        FiguraMat4 result = of();

        result.v11 = o.v11*v11+o.v12*v21+o.v13*v31+o.v14*v41;
        result.v12 = o.v11*v12+o.v12*v22+o.v13*v32+o.v14*v42;
        result.v13 = o.v11*v13+o.v12*v23+o.v13*v33+o.v14*v43;
        result.v14 = o.v11*v14+o.v12*v24+o.v13*v34+o.v14*v44;

        result.v21 = o.v21*v11+o.v22*v21+o.v23*v31+o.v24*v41;
        result.v22 = o.v21*v12+o.v22*v22+o.v23*v32+o.v24*v42;
        result.v23 = o.v21*v13+o.v22*v23+o.v23*v33+o.v24*v43;
        result.v24 = o.v21*v14+o.v22*v24+o.v23*v34+o.v24*v44;

        result.v31 = o.v31*v11+o.v32*v21+o.v33*v31+o.v34*v41;
        result.v32 = o.v31*v12+o.v32*v22+o.v33*v32+o.v34*v42;
        result.v33 = o.v31*v13+o.v32*v23+o.v33*v33+o.v34*v43;
        result.v34 = o.v31*v14+o.v32*v24+o.v33*v34+o.v34*v44;

        result.v41 = o.v41*v11+o.v42*v21+o.v43*v31+o.v44*v41;
        result.v42 = o.v41*v12+o.v42*v22+o.v43*v32+o.v44*v42;
        result.v43 = o.v41*v13+o.v42*v23+o.v43*v33+o.v44*v43;
        result.v44 = o.v41*v14+o.v42*v24+o.v43*v34+o.v44*v44;

        return result;
    }

    public FiguraVec4 times(FiguraVec4 vec) {
        FiguraVec4 result = FiguraVec4.of();
        result.x = v11*vec.x+v12*vec.y+v13*vec.z+v14*vec.w;
        result.y = v21*vec.x+v22*vec.y+v23*vec.z+v24*vec.w;
        result.z = v31*vec.x+v32*vec.y+v33*vec.z+v34*vec.w;
        result.w = v41*vec.x+v42*vec.y+v43*vec.z+v44*vec.w;
        return result;
    }

    // METAMETHODS
    //----------------------------------------------------------------
    @LuaWhitelist
    public static FiguraMat4 __add(@LuaNotNil FiguraMat4 arg1, @LuaNotNil FiguraMat4 arg2) {
        return arg1.plus(arg2);
    }
    @LuaWhitelist
    public static FiguraMat4 __sub(@LuaNotNil FiguraMat4 arg1, @LuaNotNil FiguraMat4 arg2) {
        return arg1.minus(arg2);
    }
    @LuaWhitelist
    public static FiguraMat4 __mul(@LuaNotNil FiguraMat4 arg1, @LuaNotNil FiguraMat4 arg2) {
        return arg2.times(arg1);
    }
    @LuaWhitelist
    public static FiguraVec4 __mul(@LuaNotNil FiguraMat4 arg1, @LuaNotNil FiguraVec4 arg2) {
        return arg1.times(arg2);
    }
    @LuaWhitelist
    public static boolean __eq(@LuaNotNil FiguraMat4 arg1, @LuaNotNil FiguraMat4 arg2) {
        return arg1.equals(arg2);
    }
    @LuaWhitelist
    public static int __len(@LuaNotNil FiguraMat4 arg1) {
        return 4;
    }
    @LuaWhitelist
    public static String __tostring(FiguraMat4 arg1) {
        return arg1.toString();
    }
    @LuaWhitelist
    public static Object __index(FiguraMat4 arg1, String arg2) {
        return switch (arg2) {
            case "1" -> arg1.getCol1();
            case "2" -> arg1.getCol2();
            case "3" -> arg1.getCol3();
            case "4" -> arg1.getCol4();
            default -> null;
        };
    }

    //----------------------------------------------------------------

    // REGULAR LUA METHODS
    //----------------------------------------------------------------

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.reset"
    )
    public static void reset(@LuaNotNil FiguraMat4 mat) {
        mat.reset();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.det"
    )
    public static double det(@LuaNotNil FiguraMat4 mat) {
        return mat.det();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.invert"
    )
    public static void invert(@LuaNotNil FiguraMat4 mat) {
        mat.invert();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.get_inverse"
    )
    public static FiguraMat4 getInverse(@LuaNotNil FiguraMat4 mat) {
        return mat.inverted();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.transpose"
    )
    public static void transpose(@LuaNotNil FiguraMat4 mat) {
        mat.transpose();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "mat"
            ),
            description = "matrix_n.get_transpose"
    )
    public static FiguraMat4 getTranspose(@LuaNotNil FiguraMat4 mat) {
        return mat.transposed();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat4.class, FiguraVec3.class},
                            argumentNames = {"mat", "vec"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat4.class, Double.class, Double.class, Double.class},
                            argumentNames = {"mat", "x", "y", "z"}
                    )
            },
            description = "matrix4.translate"
    )
    public static void translate(@LuaNotNil FiguraMat4 mat, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("translate", x, y, z);
        mat.translate(vec.x, vec.y, vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat4.class, FiguraVec3.class},
                            argumentNames = {"mat", "vec"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat4.class, Double.class, Double.class, Double.class},
                            argumentNames = {"mat", "x", "y", "z"}
                    )
            },
            description = "matrix4.rotate"
    )
    public static void rotate(@LuaNotNil FiguraMat4 mat, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("rotate", x, y, z);
        mat.rotateZYX(vec.x, vec.y, vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat4.class, FiguraVec3.class},
                            argumentNames = {"mat", "vec"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraMat4.class, Double.class, Double.class, Double.class},
                            argumentNames = {"mat", "x", "y", "z"}
                    )
            },
            description = "matrix_n.scale"
    )
    public static void scale(@LuaNotNil FiguraMat4 mat, Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("scale", x, y, z, 1, 1, 1);
        mat.scale(vec.x, vec.y, vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraMat4.class, Integer.class},
                    argumentNames = {"mat", "col"}
            ),
            description = "matrix4.get_column"
    )
    public static FiguraVec4 getColumn(@LuaNotNil FiguraMat4 mat, @LuaNotNil Integer column) {
        if (column <= 0 || column > 4) throw new IllegalArgumentException("Column " + column + " does not exist in a 4x4 matrix!");
        return switch (column) {
            case 1 -> mat.getCol1();
            case 2 -> mat.getCol2();
            case 3 -> mat.getCol3();
            case 4 -> mat.getCol4();
            default -> null;
        };
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {FiguraMat4.class, Integer.class},
                    argumentNames = {"mat", "row"}
            ),
            description = "matrix4.get_row"
    )
    public static FiguraVec4 getRow(@LuaNotNil FiguraMat4 mat, @LuaNotNil Integer row) {
        if (row <= 0 || row > 4) throw new IllegalArgumentException("Row " + row + " does not exist in a 4x4 matrix!");
        return switch (row) {
            case 1 -> mat.getRow1();
            case 2 -> mat.getRow2();
            case 3 -> mat.getRow3();
            case 4 -> mat.getRow4();
            default -> null;
        };
    }
}
