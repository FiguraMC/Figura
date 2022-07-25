package org.moon.figura.math.newmatrix;

import com.mojang.math.Matrix3f;
import org.luaj.vm2.LuaError;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.newvector.FiguraVec3;
import org.moon.figura.math.newvector.FiguraVec4;
import org.moon.figura.newlua.LuaWhitelist;
import org.moon.figura.newlua.docs.LuaFunctionOverload;
import org.moon.figura.newlua.docs.LuaMethodDoc;
import org.moon.figura.utils.caching.CacheStack;
import org.moon.figura.utils.caching.CacheUtils;

import java.nio.FloatBuffer;

public class FiguraMat3 extends FiguraMatrix<FiguraMat3, FiguraVec3> {

    private static final FloatBuffer copyingBuffer = BufferUtils.createFloatBuffer(3*3);
    public static FiguraMat3 fromMatrix3f(Matrix3f mat) {
        copyingBuffer.clear();
        mat.store(copyingBuffer);
        return of(copyingBuffer.get(), copyingBuffer.get(), copyingBuffer.get(),
                copyingBuffer.get(), copyingBuffer.get(), copyingBuffer.get(),
                copyingBuffer.get(), copyingBuffer.get(), copyingBuffer.get());
    }
    public Matrix3f toMatrix3f() {
        copyingBuffer.clear();
        copyingBuffer
                .put((float) v11).put((float) v21).put((float) v31)
                .put((float) v12).put((float) v22).put((float) v32)
                .put((float) v13).put((float) v23).put((float) v33);
        Matrix3f result = new Matrix3f();
        result.load(copyingBuffer);
        return result;
    }





    //Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    public double v11, v12, v13, v21, v22, v23, v31, v32, v33;

    @Override
    public void resetIdentity() {
        v12=v13=v21=v23=v31=v32 = 0;
        v11=v22=v33 = 1;
    }
    @Override
    public CacheUtils.Cache<FiguraMat3> getCache() {
        return CACHE;
    }
    private static final CacheUtils.Cache<FiguraMat3> CACHE = CacheUtils.getCache(FiguraMat3::new, 100);
    public static FiguraMat3 of() {
        return CACHE.getFresh();
    }
    public static FiguraMat3 of(double n11, double n21, double n31,
                                double n12, double n22, double n32,
                                double n13, double n23, double n33) {
        return of().set(n11, n21, n31, n12, n22, n32, n13, n23, n33);
    }
    public static class Stack extends CacheStack<FiguraMat3, FiguraMat3> {
        public Stack() {
            this(CACHE);
        }
        public Stack(CacheUtils.Cache<FiguraMat3> cache) {
            super(cache);
        }
        @Override
        protected void modify(FiguraMat3 valueToModify, FiguraMat3 modifierArg) {
            valueToModify.rightMultiply(modifierArg);
        }
        @Override
        protected void copy(FiguraMat3 from, FiguraMat3 to) {
            to.set(from);
        }
    }

    @Override
    protected double calculateDeterminant() {
        double sub11 = v22 * v33 - v23 * v32;
        double sub12 = v21 * v33 - v23 * v31;
        double sub13 = v21 * v32 - v22 * v31;
        return v11 * sub11 - v12 * sub12 + v13 * sub13;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.copy"
    )
    public FiguraMat3 copy() {
        return of(v11, v21, v31, v12, v22, v32, v13, v23, v33);
    }

    @Override
    public boolean equals(FiguraMat3 o) {
        return
                v11 == o.v11 && v12 == o.v12 && v13 == o.v13 &&
                v21 == o.v21 && v22 == o.v22 && v23 == o.v23 &&
                v31 == o.v31 && v32 == o.v32 && v33 == o.v33;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraMat3 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "\n[  " +
                (float) v11 + ", " + (float) v12 + ", " + (float) v13 + ", " +
                "\n   " + (float) v21 + ", " + v22 + ", " + (float) v23 + ", " +
                "\n   " + (float) v31 + ", " + (float) v32 + ", " + (float) v33 +
                "  ]";
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "col"
            ),
            description = "matrix_n.get_column"
    )
    public FiguraVec3 getColumn(int col) {
        return switch (col) {
            case 1 -> FiguraVec3.of(v11, v21, v31);
            case 2 -> FiguraVec3.of(v12, v22, v32);
            case 3 -> FiguraVec3.of(v13, v23, v33);
            default -> throw new LuaError("Column must be 1 to " + cols());
        };
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "row"
            ),
            description = "matrix_n.get_row"
    )
    public FiguraVec3 getRow(int row) {
        return switch (row) {
            case 1 -> FiguraVec3.of(v11, v12, v13);
            case 2 -> FiguraVec3.of(v21, v22, v23);
            case 3 -> FiguraVec3.of(v31, v32, v33);
            default -> throw new LuaError("Row must be 1 to " + rows());
        };
    }

    @Override
    public int rows() {
        return 3;
    }

    @Override
    public int cols() {
        return 3;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.set"
    )
    public FiguraMat3 set(FiguraMat3 o) {
        return set(o.v11, o.v21, o.v31, o.v12, o.v22, o.v32, o.v13, o.v23, o.v33);
    }

    public FiguraMat3 set(double n11, double n21, double n31,
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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.multiply"
    )
    public FiguraMat3 multiply(FiguraMat3 o) {
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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.right_multiply"
    )
    public FiguraMat3 rightMultiply(FiguraMat3 o) {
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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.transpose"
    )
    public FiguraMat3 transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        temp = v13; v13 = v31; v31 = temp;
        temp = v23; v23 = v32; v32 = temp;
        cachedInverse = null; //transposing doesn't invalidate the determinant
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.transposed"
    )
    public FiguraMat3 transposed() {
        return super.transposed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.invert"
    )
    public FiguraMat3 invert() {
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
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.inverted"
    )
    public FiguraMat3 inverted() {
        return super.inverted();
    }

}
