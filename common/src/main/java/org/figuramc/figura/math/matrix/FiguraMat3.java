package org.figuramc.figura.math.matrix;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.LuaUtils;
import org.joml.Matrix3f;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(
        name = "Matrix3",
        value = "matrix3"
)
public class FiguraMat3 extends FiguraMatrix<FiguraMat3, FiguraVec3> {

    public FiguraMat3 set(Matrix3f mat) {
        return set(
                mat.m00(), mat.m01(), mat.m02(),
                mat.m10(), mat.m11(), mat.m12(),
                mat.m20(), mat.m21(), mat.m22()
        );
    }

    public Matrix3f toMatrix3f() {
        return new Matrix3f(
                (float) v11, (float) v21, (float) v31,
                (float) v12, (float) v22, (float) v32,
                (float) v13, (float) v23, (float) v33
        );
    }

    public void copyDataTo(Matrix3f vanillaMatrix) {
        vanillaMatrix.set(
                (float) v11, (float) v21, (float) v31,
                (float) v12, (float) v22, (float) v32,
                (float) v13, (float) v23, (float) v33
        );
    }

    // ----------------------------IMPLEMENTATION BELOW-----------------------// 

    // Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    public double v11 = 1, v12, v13, v21, v22 = 1, v23, v31, v32, v33 = 1;

    public static FiguraMat3 of() {
        return new FiguraMat3();
    }
    public static FiguraMat3 of(double n11, double n21, double n31,
                                double n12, double n22, double n32,
                                double n13, double n23, double n33) {
        return of().set(n11, n21, n31, n12, n22, n32, n13, n23, n33);
    }

    @Override
    public void resetIdentity() {
        v12 = v13 = v21 = v23 = v31 = v32 = 0;
        v11 = v22 = v33 = 1;
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
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.copy"
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
        return getString(v11, v12, v13, v21, v22, v23, v31, v32, v33);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "col",
                    returnType = FiguraVec3.class
            ),
            value = "matrix_n.get_column"
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
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "row",
                    returnType = FiguraVec3.class
            ),
            value = "matrix_n.get_row"
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

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other",
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.set"
    )
    public FiguraMat3 set(@LuaNotNil FiguraMat3 o) {
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
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other",
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.multiply"
    )
    public FiguraMat3 multiply(@LuaNotNil FiguraMat3 o) {
        double nv11 = o.v11 * v11 + o.v12 * v21 + o.v13 * v31;
        double nv12 = o.v11 * v12 + o.v12 * v22 + o.v13 * v32;
        double nv13 = o.v11 * v13 + o.v12 * v23 + o.v13 * v33;

        double nv21 = o.v21 * v11 + o.v22 * v21 + o.v23 * v31;
        double nv22 = o.v21 * v12 + o.v22 * v22 + o.v23 * v32;
        double nv23 = o.v21 * v13 + o.v22 * v23 + o.v23 * v33;

        double nv31 = o.v31 * v11 + o.v32 * v21 + o.v33 * v31;
        double nv32 = o.v31 * v12 + o.v32 * v22 + o.v33 * v32;
        double nv33 = o.v31 * v13 + o.v32 * v23 + o.v33 * v33;

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
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other",
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.right_multiply"
    )
    public FiguraMat3 rightMultiply(@LuaNotNil FiguraMat3 o) {
        double nv11 = v11 * o.v11 + v12 * o.v21 + v13 * o.v31;
        double nv12 = v11 * o.v12 + v12 * o.v22 + v13 * o.v32;
        double nv13 = v11 * o.v13 + v12 * o.v23 + v13 * o.v33;

        double nv21 = v21 * o.v11 + v22 * o.v21 + v23 * o.v31;
        double nv22 = v21 * o.v12 + v22 * o.v22 + v23 * o.v32;
        double nv23 = v21 * o.v13 + v22 * o.v23 + v23 * o.v33;

        double nv31 = v31 * o.v11 + v32 * o.v21 + v33 * o.v31;
        double nv32 = v31 * o.v12 + v32 * o.v22 + v33 * o.v32;
        double nv33 = v31 * o.v13 + v32 * o.v23 + v33 * o.v33;

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
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.transpose"
    )
    public FiguraMat3 transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        temp = v13; v13 = v31; v31 = temp;
        temp = v23; v23 = v32; v32 = temp;
        cachedInverse = null; // transposing doesn't invalidate the determinant
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.transposed"
    )
    public FiguraMat3 transposed() {
        return super.transposed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.invert"
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
            det = 1 / det;
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

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.inverted"
    )
    public FiguraMat3 inverted() {
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
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.reset"
    )
    public FiguraMat3 reset() {
        return super.reset();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other",
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.add"
    )
    public FiguraMat3 add(@LuaNotNil FiguraMat3 o) {
        v11 += o.v11;
        v12 += o.v12;
        v13 += o.v13;
        v21 += o.v21;
        v22 += o.v22;
        v23 += o.v23;
        v31 += o.v31;
        v32 += o.v32;
        v33 += o.v33;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat3.class,
                    argumentNames = "other",
                    returnType = FiguraMat3.class
            ),
            value = "matrix_n.sub"
    )
    public FiguraMat3 sub(@LuaNotNil FiguraMat3 o) {
        v11 -= o.v11;
        v12 -= o.v12;
        v13 -= o.v13;
        v21 -= o.v21;
        v22 -= o.v22;
        v23 -= o.v23;
        v31 -= o.v31;
        v32 -= o.v32;
        v33 -= o.v33;
        invalidate();
        return this;
    }

    public FiguraMat3 scale(double x, double y, double z) {
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
        return this;
    }

    public FiguraMat3 scale(FiguraVec3 vec) {
        return scale(vec.x, vec.y, vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "matrix_n.scale"
    )
    public FiguraMat3 scale(Object x, Double y, Double z) {
        return scale(LuaUtils.parseOneArgVec("scale", x, y, z, 1d));
    }

    public FiguraMat3 translate(double x, double y) {
        v11 += x * v31;
        v12 += x * v32;
        v13 += x * v33;

        v21 += y * v31;
        v22 += y * v32;
        v23 += y * v33;
        invalidate();
        return this;
    }
    public FiguraMat3 translate(FiguraVec2 amount) {
        return translate(amount.x, amount.y);
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
            value = "matrix_n.translate"
    )
    public FiguraMat3 translate(Object x, Double y) {
        return translate(LuaUtils.parseVec2("translate", x, y));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "degrees"
            ),
            value = "matrix_n.rotate_x"
    )
    public FiguraMat3 rotateX(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv21 = c * v21 - s * v31;
        double nv22 = c * v22 - s * v32;
        double nv23 = c * v23 - s * v33;

        v31 = s * v21 + c * v31;
        v32 = s * v22 + c * v32;
        v33 = s * v23 + c * v33;

        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        invalidate();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "degrees"
            ),
            value = "matrix_n.rotate_y"
    )
    public FiguraMat3 rotateY(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c * v11 + s * v31;
        double nv12 = c * v12 + s * v32;
        double nv13 = c * v13 + s * v33;

        v31 = c * v31 - s * v11;
        v32 = c * v32 - s * v12;
        v33 = c * v33 - s * v13;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        invalidate();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "degrees"
            ),
            value = "matrix_n.rotate_z"
    )
    public FiguraMat3 rotateZ(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c * v11 - s * v21;
        double nv12 = c * v12 - s * v22;
        double nv13 = c * v13 - s * v23;

        v21 = c * v21 + s * v11;
        v22 = c * v22 + s * v12;
        v23 = c * v23 + s * v13;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        invalidate();
        return this;
    }

    // Rotates using ZYX matrix order, meaning the X axis, then Y, then Z.
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

        double bc = b * c;
        double ac = a * c;
        double ce = c * e;
        double cf = c * f;
        double p1 = (b * d * e - a * f);
        double p2 = (a * d * e + b * f);
        double p3 = (a * e + b * d * f);
        double p4 = (a * d * f - b * e);

        double nv11 = ce * v11 + p1 * v21 + p2 * v31;
        double nv21 = cf * v11 + p3 * v21 + p4 * v31;
        double nv31 = -d * v11 + bc * v21 + ac * v31;

        double nv12 = ce * v12 + p1 * v22 + p2 * v32;
        double nv22 = cf * v12 + p3 * v22 + p4 * v32;
        double nv32 = -d * v12 + bc * v22 + ac * v32;

        double nv13 = ce * v13 + p1 * v23 + p2 * v33;
        double nv23 = cf * v13 + p3 * v23 + p4 * v33;
        double nv33 = -d * v13 + bc * v23 + ac * v33;

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

    public void rotateZYX(FiguraVec3 vec) {
        rotateZYX(vec.x, vec.y, vec.z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec",
                            returnType = FiguraMat3.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraMat3.class
                    )
            },
            value = "matrix_n.rotate"
    )
    public FiguraMat3 rotate(Object x, Double y, Double z) {
        rotateZYX(LuaUtils.parseVec3("rotate", x, y, z));
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc("matrix_n.deaugmented")
    public FiguraMat2 deaugmented() {
        FiguraMat2 result = FiguraMat2.of();
        result.set(v11, v21, v12, v22);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc("matrix_n.augmented")
    public FiguraMat4 augmented() {
        FiguraMat4 result = FiguraMat4.of();
        result.set(v11, v21, v31, 0, v12, v22, v32, 0, v13, v23, v33, 0, 0, 0, 0, 1);
        return result;
    }

    public FiguraVec2 apply(FiguraVec3 vec) {
        FiguraVec3 result = this.times(vec);
        return FiguraVec2.of(result.x, result.y);
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
            value = "matrix_n.apply"
    )
    public FiguraVec2 apply(Object x, Double y) {
        FiguraVec2 vec = LuaUtils.parseVec2("apply", x, y);
        return apply(FiguraVec3.of(vec.x, vec.y, 1));
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
            value = "matrix_n.apply_dir"
    )
    public FiguraVec2 applyDir(Object x, Double y, Double z) {
        FiguraVec2 vec = LuaUtils.parseVec2("applyDir", x, y);
        return apply(FiguraVec3.of(vec.x, vec.y, 0));
    }

    // -----------------------------METAMETHODS-----------------------------------// 

    @LuaWhitelist
    public FiguraMat3 __add(@LuaNotNil FiguraMat3 mat) {
        return this.plus(mat);
    }
    @LuaWhitelist
    public FiguraMat3 __sub(@LuaNotNil FiguraMat3 mat) {
        return this.minus(mat);
    }
    @LuaWhitelist
    public Object __mul(@LuaNotNil Object o) {
        if (o instanceof FiguraMat3 mat)
            return mat.times(this);
        else if (o instanceof FiguraVec3 vec)
            return this.times(vec);
        else if (o instanceof Number n)
            return this.copy().scale(n.doubleValue(), n.doubleValue(), n.doubleValue());

        throw new LuaError("Invalid types to Matrix3 __mul: " + o.getClass().getSimpleName());
    }
    @LuaWhitelist
    public boolean __eq(Object o) {
        return this.equals(o);
    }
    @LuaWhitelist
    public int __len() {
        return 3;
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
            case "3", "c3" -> this.getColumn(3);

            case "r1" -> this.getRow(1);
            case "r2" -> this.getRow(2);
            case "r3" -> this.getRow(3);

            case "v11" -> this.v11;
            case "v12" -> this.v12;
            case "v13" -> this.v13;
            case "v21" -> this.v21;
            case "v22" -> this.v22;
            case "v23" -> this.v23;
            case "v31" -> this.v31;
            case "v32" -> this.v32;
            case "v33" -> this.v33;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String string, Object value) {
        if (value instanceof FiguraVec3 vec3) {
            switch (string) {
                case "1", "c1" -> {
                    v11 = vec3.x; v21 = vec3.y; v31 = vec3.z;
                }
                case "2", "c2" -> {
                    v12 = vec3.x; v22 = vec3.y; v32 = vec3.z;
                }
                case "3", "c3" -> {
                    v13 = vec3.x; v23 = vec3.y; v33 = vec3.z;
                }
                case "r1" -> {
                    v11 = vec3.x; v12 = vec3.y; v13 = vec3.z;
                }
                case "r2" -> {
                    v21 = vec3.x; v22 = vec3.y; v23 = vec3.z;
                }
                case "r3" -> {
                    v31 = vec3.x; v32 = vec3.y; v33 = vec3.z;
                }
            }
            return;
        }
        if (value instanceof Number num) {
            switch (string) {
                case "v11" -> this.v11 = num.doubleValue();
                case "v12" -> this.v12 = num.doubleValue();
                case "v13" -> this.v13 = num.doubleValue();
                case "v21" -> this.v21 = num.doubleValue();
                case "v22" -> this.v22 = num.doubleValue();
                case "v23" -> this.v23 = num.doubleValue();
                case "v31" -> this.v31 = num.doubleValue();
                case "v32" -> this.v32 = num.doubleValue();
                case "v33" -> this.v33 = num.doubleValue();
            }
            return;
        }
        throw new LuaError("Illegal arguments to Matrix3 __newindex: " + string + ", " + value);
    }
}
