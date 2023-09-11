package org.figuramc.figura.math.matrix;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(
        name = "Matrix2",
        value = "matrix2"
)
public class FiguraMat2 extends FiguraMatrix<FiguraMat2, FiguraVec2> {

    // Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    public double v11 = 1, v12, v21, v22 = 1;

    public static FiguraMat2 of() {
        return new FiguraMat2();
    }
    public static FiguraMat2 of(double n11, double n21,
                                double n12, double n22) {
        return of().set(n11, n21, n12, n22);
    }

    @Override
    public void resetIdentity() {
        v12 = v21 = 0;
        v11 = v22 = 1;
    }

    @Override
    protected double calculateDeterminant() {
        return v11 * v22 - v12 * v21;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.copy"
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
        return getString(v11, v12, v21, v22);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "col",
                    returnType = FiguraVec2.class
            ),
            value = "matrix_n.get_column"
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
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "row",
                    returnType = FiguraVec2.class
            ),
            value = "matrix_n.get_row"
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

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other",
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.set"
    )
    public FiguraMat2 set(@LuaNotNil FiguraMat2 o) {
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
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other",
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.multiply"
    )
    public FiguraMat2 multiply(@LuaNotNil FiguraMat2 o) {
        double nv11 = o.v11 * v11 + o.v12 * v21;
        double nv12 = o.v11 * v12 + o.v12 * v22;

        double nv21 = o.v21 * v11 + o.v22 * v21;
        double nv22 = o.v21 * v12 + o.v22 * v22;

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
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other",
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.right_multiply"
    )
    public FiguraMat2 rightMultiply(@LuaNotNil FiguraMat2 o) {
        double nv11 = v11 * o.v11 + v12 * o.v21;
        double nv12 = v11 * o.v12 + v12 * o.v22;

        double nv21 = v21 * o.v11 + v22 * o.v21;
        double nv22 = v21 * o.v12 + v22 * o.v22;

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
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.transpose"
    )
    public FiguraMat2 transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        cachedInverse = null; // transposing doesn't invalidate the determinant
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.transposed"
    )
    public FiguraMat2 transposed() {
        return super.transposed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.invert"
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

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.inverted"
    )
    public FiguraMat2 inverted() {
        return super.inverted();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("matrix_n.det")
    public double det() {
        return super.det();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.reset"
    )
    public FiguraMat2 reset() {
        return super.reset();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other",
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.add")
    public FiguraMat2 add(@LuaNotNil FiguraMat2 o) {
        v11 += o.v11;
        v12 += o.v12;
        v21 += o.v21;
        v22 += o.v22;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat2.class,
                    argumentNames = "other",
                    returnType = FiguraMat2.class
            ),
            value = "matrix_n.sub"
    )
    public FiguraMat2 sub(@LuaNotNil FiguraMat2 o) {
        v11 -= o.v11;
        v12 -= o.v12;
        v21 -= o.v21;
        v22 -= o.v22;
        invalidate();
        return this;
    }

    public FiguraMat2 scale(double x, double y) {
        v11 *= x;
        v12 *= x;
        v21 *= y;
        v22 *= y;
        invalidate();
        return this;
    }

    public FiguraMat2 scale(FiguraVec2 vec) {
        return scale(vec.x, vec.y);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"}
                    )
            },
            value = "matrix_n.scale"
    )
    public FiguraMat2 scale(Object x, Double y) {
        return scale(LuaUtils.parseVec2("scale", x, y, 1, 1));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "degrees"
            ),
            value = "matrix_n.rotate"
    )
    public FiguraMat2 rotate(Double degrees) {
        if (degrees != null) {
            degrees = Math.toRadians(degrees);
            double c = Math.cos(degrees);
            double s = Math.sin(degrees);

            double nv11 = c * v11 - s * v21;
            double nv12 = c * v12 - s * v22;

            v21 = c * v21 + s * v11;
            v22 = c * v22 + s * v12;

            v11 = nv11;
            v12 = nv12;
        }
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("matrix_n.augmented")
    public FiguraMat3 augmented() {
        FiguraMat3 result = FiguraMat3.of();
        result.set(v11, v21, 0, v12, v22, 0, 0, 0, 1);
        return result;
    }

    public double apply(FiguraVec2 vec) {
        FiguraVec2 result = this.times(vec);
        return result.x;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "x"
            ),
            value = "matrix_n.apply"
    )
    public double apply(double x) {
        return apply(FiguraVec2.of(x, 1));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "x"
            ),
            value = "matrix_n.apply_dir"
    )
    public double applyDir(double x) {
        return apply(FiguraVec2.of(x, 0));
    }

    // -----------------------------METAMETHODS-----------------------------------// 

    @LuaWhitelist
    public FiguraMat2 __add(@LuaNotNil FiguraMat2 mat) {
        return this.plus(mat);
    }
    @LuaWhitelist
    public FiguraMat2 __sub(@LuaNotNil FiguraMat2 mat) {
        return this.minus(mat);
    }
    @LuaWhitelist
    public Object __mul(@LuaNotNil Object o) {
        if (o instanceof FiguraMat2 mat)
            return mat.times(this);
        else if (o instanceof FiguraVec2 vec)
            return this.times(vec);
        else if (o instanceof Number n)
            return this.copy().scale(n.doubleValue(), n.doubleValue());

        throw new LuaError("Invalid types to Matrix2 __mul: " + o.getClass().getSimpleName());
    }
    @LuaWhitelist
    public boolean __eq(Object o) {
        return this.equals(o);
    }
    @LuaWhitelist
    public int __len() {
        return 2;
    }
    @LuaWhitelist
    public String __tostring() {
        return this.toString();
    }
    @LuaWhitelist
    public Object __index(String string) {
        if (string == null)
            return null;
        return switch (string) {
            case "1", "c1" -> this.getColumn(1);
            case "2", "c2" -> this.getColumn(2);

            case "r1" -> this.getRow(1);
            case "r2" -> this.getRow(2);

            case "v11" -> this.v11;
            case "v12" -> this.v12;
            case "v21" -> this.v21;
            case "v22" -> this.v22;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String string, Object value) {
        if (value instanceof FiguraVec2 vec2) {
            switch (string) {
                case "1", "c1" -> {
                    v11 = vec2.x; v21 = vec2.y;
                }
                case "2", "c2" -> {
                    v12 = vec2.x; v22 = vec2.y;
                }
                case "r1" -> {
                    v11 = vec2.x; v12 = vec2.y;
                }
                case "r2" -> {
                    v21 = vec2.x; v22 = vec2.y;
                }
            }
            return;
        }
        if (value instanceof Number num) {
            switch (string) {
                case "v11" -> this.v11 = num.doubleValue();
                case "v12" -> this.v12 = num.doubleValue();
                case "v21" -> this.v21 = num.doubleValue();
                case "v22" -> this.v22 = num.doubleValue();
            }
            return;
        }
        throw new LuaError("Illegal arguments to Matrix2 __newindex: " + string + ", " + value);
    }
}
