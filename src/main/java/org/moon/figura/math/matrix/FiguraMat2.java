package org.moon.figura.math.matrix;

import org.luaj.vm2.LuaError;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.utils.caching.CacheStack;
import org.moon.figura.utils.caching.CacheUtils;

@LuaType(typeName = "mat2")
public class FiguraMat2 extends FiguraMatrix<FiguraMat2, FiguraVec2> {

    //Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    public double v11, v12, v21, v22;

    @Override
    public void resetIdentity() {
        v12=v21 = 0;
        v11=v22 = 1;
    }
    @Override
    public CacheUtils.Cache<FiguraMat2> getCache() {
        return CACHE;
    }
    private static final CacheUtils.Cache<FiguraMat2> CACHE = CacheUtils.getCache(FiguraMat2::new, 100);
    public static FiguraMat2 of() {
        return CACHE.getFresh();
    }
    public static FiguraMat2 of(double n11, double n21,
                                double n12, double n22) {
        return of().set(n11, n21, n12, n22);
    }
    public static class Stack extends CacheStack<FiguraMat2, FiguraMat2> {
        public Stack() {
            this(CACHE);
        }
        public Stack(CacheUtils.Cache<FiguraMat2> cache) {
            super(cache);
        }
        @Override
        protected void modify(FiguraMat2 valueToModify, FiguraMat2 modifierArg) {
            valueToModify.rightMultiply(modifierArg);
        }
        @Override
        protected void copy(FiguraMat2 from, FiguraMat2 to) {
            to.set(from);
        }
    }

    @Override
    protected double calculateDeterminant() {
        return v11 * v22 - v12 * v21;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.copy"
    )
    public FiguraMat2 copy() {
        return of(v11, v21, v12, v22);
    }

    @Override
    public boolean equals(FiguraMat2 o) {
        return
                v11 == o.v11 && v12 == o.v12 &&
                v21 == o.v21 && v22 == o.v22;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof FiguraMat2 o)
            return equals(o);
        return false;
    }
    @Override
    public String toString() {
        return "\n[  " +
                (float) v11 + ", " + (float) v12 + ", " +
                "\n   " + (float) v21 + ", " + v22 +
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
    public FiguraVec2 getColumn(int col) {
        return switch (col) {
            case 1 -> FiguraVec2.of(v11, v21);
            case 2 -> FiguraVec2.of(v12, v22);
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
    public FiguraVec2 getRow(int row) {
        return switch (row) {
            case 1 -> FiguraVec2.of(v11, v12);
            case 2 -> FiguraVec2.of(v21, v22);
            default -> throw new LuaError("Row must be 1 to " + rows());
        };
    }

    @Override
    public int rows() {
        return 2;
    }

    @Override
    public int cols() {
        return 2;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.set"
    )
    public FiguraMat2 set(FiguraMat2 o) {
        return set(o.v11, o.v21, o.v12, o.v22);
    }

    public FiguraMat2 set(double n11, double n21,
                          double n12, double n22) {
        v11 = n11;
        v12 = n12;
        v21 = n21;
        v22 = n22;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.multiply"
    )
    public FiguraMat2 multiply(FiguraMat2 o) {
        double nv11 = o.v11*v11+o.v12*v21;
        double nv12 = o.v11*v12+o.v12*v22;

        double nv21 = o.v21*v11+o.v22*v21;
        double nv22 = o.v21*v12+o.v22*v22;

        v11 = nv11;
        v12 = nv12;
        v21 = nv21;
        v22 = nv22;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other"
            ),
            description = "matrix_n.right_multiply"
    )
    public FiguraMat2 rightMultiply(FiguraMat2 o) {
        double nv11 = v11*o.v11+v12*o.v21;
        double nv12 = v11*o.v12+v12*o.v22;

        double nv21 = v21*o.v11+v22*o.v21;
        double nv22 = v21*o.v12+v22*o.v22;

        v11 = nv11;
        v12 = nv12;
        v21 = nv21;
        v22 = nv22;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.transpose"
    )
    public FiguraMat2 transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        cachedInverse = null; //transposing doesn't invalidate the determinant
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.transposed"
    )
    public FiguraMat2 transposed() {
        return super.transposed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.invert"
    )
    public FiguraMat2 invert() {
        double det = det();
        return set(
                v22 / det,
                v12 / det,
                v21 / det,
                v11 / det
        );
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload,
            description = "matrix_n.inverted"
    )
    public FiguraMat2 inverted() {
        return super.inverted();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            description = "matrix_n.det"
    )
    public double det() {
        return super.det();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            description = "matrix_n.reset"
    )
    public FiguraMat2 reset() {
        return super.reset();
    }






    // STATIC CREATOR METHODS
    //----------------------------------------------------------------
    public static FiguraMat2 createScaleMatrix(double x, double y) {
        FiguraMat2 result = of();
        result.v11 = x;
        result.v22 = y;
        return result;
    }
    public static FiguraMat2 createRotationMatrix(double degrees) {
        degrees = Math.toRadians(degrees);
        double s = Math.sin(degrees);
        double c = Math.cos(degrees);
        FiguraMat2 result = of();
        result.v11 = result.v22 = c;
        result.v12 = -s;
        result.v21 = s;
        return result;
    }
}
