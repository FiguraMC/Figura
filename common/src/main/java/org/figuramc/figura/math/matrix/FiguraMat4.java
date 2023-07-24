package org.figuramc.figura.math.matrix;

import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.utils.LuaUtils;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(
        name = "Matrix4",
        value = "matrix4"
)
public class FiguraMat4 extends FiguraMatrix<FiguraMat4, FiguraVec4> {

    public FiguraMat4 set(Matrix4f mat) {
        return set(
                mat.m00(), mat.m01(), mat.m02(), mat.m03(),
                mat.m10(), mat.m11(), mat.m12(), mat.m13(),
                mat.m20(), mat.m21(), mat.m22(), mat.m23(),
                mat.m30(), mat.m31(), mat.m32(), mat.m33()
        );
    }

    public FiguraMat4 set(Matrix4d mat) {
        return set(
                mat.m00(), mat.m01(), mat.m02(), mat.m03(),
                mat.m10(), mat.m11(), mat.m12(), mat.m13(),
                mat.m20(), mat.m21(), mat.m22(), mat.m23(),
                mat.m30(), mat.m31(), mat.m32(), mat.m33()
        );
    }

    public Matrix4f toMatrix4f() {
        return new Matrix4f(
                (float) v11, (float) v21, (float) v31, (float) v41,
                (float) v12, (float) v22, (float) v32, (float) v42,
                (float) v13, (float) v23, (float) v33, (float) v43,
                (float) v14, (float) v24, (float) v34, (float) v44
        );
    }

    public void copyDataTo(Matrix4f vanillaMatrix) {
        vanillaMatrix.set(
                (float) v11, (float) v21, (float) v31, (float) v41,
                (float) v12, (float) v22, (float) v32, (float) v42,
                (float) v13, (float) v23, (float) v33, (float) v43,
                (float) v14, (float) v24, (float) v34, (float) v44
        );
    }

    // ----------------------------IMPLEMENTATION BELOW-----------------------// 

    // Values are named as v(ROW)(COLUMN), both 1-indexed like in actual math
    public double v11 = 1, v12, v13, v14, v21, v22 = 1, v23, v24, v31, v32, v33 = 1, v34, v41, v42, v43, v44 = 1;

    public static FiguraMat4 of() {
        return new FiguraMat4();
    }
    public static FiguraMat4 of(double n11, double n21, double n31, double n41,
                                double n12, double n22, double n32, double n42,
                                double n13, double n23, double n33, double n43,
                                double n14, double n24, double n34, double n44) {
        return of().set(n11, n21, n31, n41, n12, n22, n32, n42, n13, n23, n33, n43, n14, n24, n34, n44);
    }

    @Override
    public void resetIdentity() {
        v12 = v13 = v14 = v21 = v23 = v24 = v31 = v32 = v34 = v41 = v42 = v43 = 0;
        v11 = v22 = v33 = v44 = 1;
    }

    @Override
    protected double calculateDeterminant() {
        // https://stackoverflow.com/a/44446912
        var A2323 = v33 * v44 - v34 * v43 ;
        var A1323 = v32 * v44 - v34 * v42 ;
        var A1223 = v32 * v43 - v33 * v42 ;
        var A0323 = v31 * v44 - v34 * v41 ;
        var A0223 = v31 * v43 - v33 * v41 ;
        var A0123 = v31 * v42 - v32 * v41 ;

        return v11 * ( v22 * A2323 - v23 * A1323 + v24 * A1223 )
                - v12 * ( v21 * A2323 - v23 * A0323 + v24 * A0223 )
                + v13 * ( v21 * A1323 - v22 * A0323 + v24 * A0123 )
                - v14 * ( v21 * A1223 - v22 * A0223 + v23 * A0123 ) ;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.copy"
    )
    public FiguraMat4 copy() {
        return of(v11, v21, v31, v41, v12, v22, v32, v42, v13, v23, v33, v43, v14, v24, v34, v44);
    }

    @Override
    public boolean equals(FiguraMat4 o) {
        return
                v11 == o.v11 && v12 == o.v12 && v13 == o.v13 && v14 == o.v14 &&
                v21 == o.v21 && v22 == o.v22 && v23 == o.v23 && v24 == o.v24 &&
                v31 == o.v31 && v32 == o.v32 && v33 == o.v33 && v34 == o.v34 &&
                v41 == o.v41 && v42 == o.v42 && v43 == o.v43 && v44 == o.v44;
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof FiguraMat4 o && equals(o);
    }
    @Override
    public String toString() {
        return getString(v11, v12, v13, v14, v21, v22, v23, v24, v31, v32, v33, v34, v41, v42, v43, v44);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "col",
                    returnType = FiguraVec4.class
            ),
            value = "matrix_n.get_column"
    )
    public FiguraVec4 getColumn(int col) {
        return switch (col) {
            case 1 -> FiguraVec4.of(v11, v21, v31, v41);
            case 2 -> FiguraVec4.of(v12, v22, v32, v42);
            case 3 -> FiguraVec4.of(v13, v23, v33, v43);
            case 4 -> FiguraVec4.of(v14, v24, v34, v44);
            default -> throw new LuaError("Column must be 1 to " + cols());
        };
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "row",
                    returnType = FiguraVec4.class
            ),
            value = "matrix_n.get_row"
    )
    public FiguraVec4 getRow(int row) {
        return switch (row) {
            case 1 -> FiguraVec4.of(v11, v12, v13, v14);
            case 2 -> FiguraVec4.of(v21, v22, v23, v24);
            case 3 -> FiguraVec4.of(v31, v32, v33, v34);
            case 4 -> FiguraVec4.of(v41, v42, v43, v44);
            default -> throw new LuaError("Row must be 1 to " + rows());
        };
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public int cols() {
        return 4;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "other",
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.set"
    )
    public FiguraMat4 set(@LuaNotNil FiguraMat4 o) {
        return set(o.v11, o.v21, o.v31, o.v41, o.v12, o.v22, o.v32, o.v42, o.v13, o.v23, o.v33, o.v43, o.v14, o.v24, o.v34, o.v44);
    }

    public FiguraMat4 set(double n11, double n21, double n31, double n41,
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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "other",
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.multiply"
    )
    public FiguraMat4 multiply(@LuaNotNil FiguraMat4 o) {
        double nv11 = o.v11 * v11 + o.v12 * v21 + o.v13 * v31 + o.v14 * v41;
        double nv12 = o.v11 * v12 + o.v12 * v22 + o.v13 * v32 + o.v14 * v42;
        double nv13 = o.v11 * v13 + o.v12 * v23 + o.v13 * v33 + o.v14 * v43;
        double nv14 = o.v11 * v14 + o.v12 * v24 + o.v13 * v34 + o.v14 * v44;

        double nv21 = o.v21 * v11 + o.v22 * v21 + o.v23 * v31 + o.v24 * v41;
        double nv22 = o.v21 * v12 + o.v22 * v22 + o.v23 * v32 + o.v24 * v42;
        double nv23 = o.v21 * v13 + o.v22 * v23 + o.v23 * v33 + o.v24 * v43;
        double nv24 = o.v21 * v14 + o.v22 * v24 + o.v23 * v34 + o.v24 * v44;

        double nv31 = o.v31 * v11 + o.v32 * v21 + o.v33 * v31 + o.v34 * v41;
        double nv32 = o.v31 * v12 + o.v32 * v22 + o.v33 * v32 + o.v34 * v42;
        double nv33 = o.v31 * v13 + o.v32 * v23 + o.v33 * v33 + o.v34 * v43;
        double nv34 = o.v31 * v14 + o.v32 * v24 + o.v33 * v34 + o.v34 * v44;

        double nv41 = o.v41 * v11 + o.v42 * v21 + o.v43 * v31 + o.v44 * v41;
        double nv42 = o.v41 * v12 + o.v42 * v22 + o.v43 * v32 + o.v44 * v42;
        double nv43 = o.v41 * v13 + o.v42 * v23 + o.v43 * v33 + o.v44 * v43;
        v44 = o.v41 * v14 + o.v42 * v24 + o.v43 * v34 + o.v44 * v44;

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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "other",
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.right_multiply"
    )
    public FiguraMat4 rightMultiply(@LuaNotNil FiguraMat4 o) {
        double nv11 = v11 * o.v11 + v12 * o.v21 + v13 * o.v31 + v14 * o.v41;
        double nv12 = v11 * o.v12 + v12 * o.v22 + v13 * o.v32 + v14 * o.v42;
        double nv13 = v11 * o.v13 + v12 * o.v23 + v13 * o.v33 + v14 * o.v43;
        double nv14 = v11 * o.v14 + v12 * o.v24 + v13 * o.v34 + v14 * o.v44;

        double nv21 = v21 * o.v11 + v22 * o.v21 + v23 * o.v31 + v24 * o.v41;
        double nv22 = v21 * o.v12 + v22 * o.v22 + v23 * o.v32 + v24 * o.v42;
        double nv23 = v21 * o.v13 + v22 * o.v23 + v23 * o.v33 + v24 * o.v43;
        double nv24 = v21 * o.v14 + v22 * o.v24 + v23 * o.v34 + v24 * o.v44;

        double nv31 = v31 * o.v11 + v32 * o.v21 + v33 * o.v31 + v34 * o.v41;
        double nv32 = v31 * o.v12 + v32 * o.v22 + v33 * o.v32 + v34 * o.v42;
        double nv33 = v31 * o.v13 + v32 * o.v23 + v33 * o.v33 + v34 * o.v43;
        double nv34 = v31 * o.v14 + v32 * o.v24 + v33 * o.v34 + v34 * o.v44;

        double nv41 = v41 * o.v11 + v42 * o.v21 + v43 * o.v31 + v44 * o.v41;
        double nv42 = v41 * o.v12 + v42 * o.v22 + v43 * o.v32 + v44 * o.v42;
        double nv43 = v41 * o.v13 + v42 * o.v23 + v43 * o.v33 + v44 * o.v43;
        v44 = v41 * o.v14 + v42 * o.v24 + v43 * o.v34 + v44 * o.v44;

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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.transpose"
    )
    public FiguraMat4 transpose() {
        double temp;
        temp = v12; v12 = v21; v21 = temp;
        temp = v13; v13 = v31; v31 = temp;
        temp = v14; v14 = v41; v41 = temp;
        temp = v23; v23 = v32; v32 = temp;
        temp = v24; v24 = v42; v42 = temp;
        temp = v34; v34 = v43; v43 = temp;
        cachedInverse = null; // transposing doesn't invalidate the determinant
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.transposed"
    )
    public FiguraMat4 transposed() {
        return super.transposed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.invert"
    )
    public FiguraMat4 invert() {
        FiguraMat4 capture = copy();
        if (cachedInverse != null) {
            set(cachedInverse);
            cachedDeterminant = 1 / cachedDeterminant;
        } else {
            // https://stackoverflow.com/a/44446912

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
                det = Double.MIN_VALUE; // Prevent divide by 0 errors

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
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.inverted"
    )
    public FiguraMat4 inverted() {
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
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.reset"
    )
    public FiguraMat4 reset() {
        return super.reset();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "other",
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.add"
    )
    public FiguraMat4 add(@LuaNotNil FiguraMat4 o) {
        v11 += o.v11;
        v12 += o.v12;
        v13 += o.v13;
        v14 += o.v14;
        v21 += o.v21;
        v22 += o.v22;
        v23 += o.v23;
        v24 += o.v24;
        v31 += o.v31;
        v32 += o.v32;
        v33 += o.v33;
        v34 += o.v34;
        v41 += o.v41;
        v42 += o.v42;
        v43 += o.v43;
        v44 += o.v44;
        invalidate();
        return this;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = FiguraMat4.class,
                    argumentNames = "other",
                    returnType = FiguraMat4.class
            ),
            value = "matrix_n.sub"
    )
    public FiguraMat4 sub(@LuaNotNil FiguraMat4 o) {
        v11 -= o.v11;
        v12 -= o.v12;
        v13 -= o.v13;
        v14 -= o.v14;
        v21 -= o.v21;
        v22 -= o.v22;
        v23 -= o.v23;
        v24 -= o.v24;
        v31 -= o.v31;
        v32 -= o.v32;
        v33 -= o.v33;
        v34 -= o.v34;
        v41 -= o.v41;
        v42 -= o.v42;
        v43 -= o.v43;
        v44 -= o.v44;
        invalidate();
        return this;
    }

    public FiguraMat4 scale(double x, double y, double z) {
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
        return this;
    }


    public FiguraMat4 scale(FiguraVec3 vec) {
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
    public FiguraMat4 scale(Object x, Double y, Double z) {
        return scale(LuaUtils.parseOneArgVec("scale", x, y, z, 1d));
    }

    public FiguraMat4 translate(double x, double y, double z) {
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
        return this;
    }
    public FiguraMat4 translate(FiguraVec3 amount) {
        return translate(amount.x, amount.y, amount.z);
    }

    public FiguraMat4 translate(Vec3 amount) {
        return translate(amount.x, amount.y, amount.z);
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
            value = "matrix_n.translate"
    )
    public FiguraMat4 translate(Object x, Double y, Double z) {
        return translate(LuaUtils.parseVec3("translate", x, y, z));
    }

    // Meant to mimic PoseStack's translate() behavior. This effectively right-multiplies by a translation matrix.
    public FiguraMat4 translateFirst(double x, double y, double z) {
        v14 += v11 * x + v12 * y + v13 * z;
        v24 += v21 * x + v22 * y + v23 * z;
        v34 += v31 * x + v32 * y + v33 * z;
        v44 += v41 * x + v42 * y + v43 * z;
        invalidate();
        return this;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "degrees"
            ),
            value = "matrix_n.rotate_x"
    )
    public FiguraMat4 rotateX(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv21 = c * v21 - s * v31;
        double nv22 = c * v22 - s * v32;
        double nv23 = c * v23 - s * v33;
        double nv24 = c * v24 - s * v34;

        v31 = s * v21 + c * v31;
        v32 = s * v22 + c * v32;
        v33 = s * v23 + c * v33;
        v34 = s * v24 + c * v34;

        v21 = nv21;
        v22 = nv22;
        v23 = nv23;
        v24 = nv24;
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
    public FiguraMat4 rotateY(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c * v11 + s * v31;
        double nv12 = c * v12 + s * v32;
        double nv13 = c * v13 + s * v33;
        double nv14 = c * v14 + s * v34;

        v31 = c * v31 - s * v11;
        v32 = c * v32 - s * v12;
        v33 = c * v33 - s * v13;
        v34 = c * v34 - s * v14;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v14 = nv14;
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
    public FiguraMat4 rotateZ(double degrees) {
        degrees = Math.toRadians(degrees);
        double c = Math.cos(degrees);
        double s = Math.sin(degrees);

        double nv11 = c * v11 - s * v21;
        double nv12 = c * v12 - s * v22;
        double nv13 = c * v13 - s * v23;
        double nv14 = c * v14 - s * v24;

        v21 = c * v21 + s * v11;
        v22 = c * v22 + s * v12;
        v23 = c * v23 + s * v13;
        v24 = c * v24 + s * v14;

        v11 = nv11;
        v12 = nv12;
        v13 = nv13;
        v14 = nv14;
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

        double nv14 = ce * v14 + p1 * v24 + p2 * v34;
        double nv24 = cf * v14 + p3 * v24 + p4 * v34;
        v34 = -d * v14 + bc * v24 + ac * v34;

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

    public void rotateZYX(FiguraVec3 vec) {
        rotateZYX(vec.x, vec.y, vec.z);
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
            value = "matrix_n.rotate"
    )
    public FiguraMat4 rotate(Object x, Double y, Double z) {
        rotateZYX(LuaUtils.parseVec3("rotate", x, y, z));
        return this;
    }


    @LuaWhitelist
    @LuaMethodDoc("matrix_n.deaugmented")
    public FiguraMat3 deaugmented() {
        FiguraMat3 result = FiguraMat3.of();
        result.set(v11, v21, v31, v12, v22, v32, v13, v23, v33);
        return result;
    }

    private FiguraMat4 rawScale(double f) {
        v11 *= f;
        v12 *= f;
        v13 *= f;
        v14 *= f;
        v21 *= f;
        v22 *= f;
        v23 *= f;
        v24 *= f;
        v31 *= f;
        v32 *= f;
        v33 *= f;
        v34 *= f;
        v41 *= f;
        v42 *= f;
        v43 *= f;
        v44 *= f;
        invalidate();
        return this;
    }

    public FiguraVec3 apply(FiguraVec4 vec) {
        FiguraVec4 result = this.times(vec);
        return FiguraVec3.of(result.x, result.y, result.z);
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
            value = "matrix_n.apply"
    )
    public FiguraVec3 apply(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("apply", x, y, z);
        return apply(FiguraVec4.of(vec.x, vec.y, vec.z, 1));
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
            value = "matrix_n.apply_dir"
    )
    public FiguraVec3 applyDir(Object x, Double y, Double z) {
        FiguraVec3 vec = LuaUtils.parseVec3("applyDir", x, y, z);
        return apply(FiguraVec4.of(vec.x, vec.y, vec.z, 0));
    }

    // -----------------------------METAMETHODS-----------------------------------// 

    @LuaWhitelist
    public FiguraMat4 __add(@LuaNotNil FiguraMat4 mat) {
        return this.plus(mat);
    }
    @LuaWhitelist
    public FiguraMat4 __sub(@LuaNotNil FiguraMat4 mat) {
        return this.minus(mat);
    }
    @LuaWhitelist
    public Object __mul(@LuaNotNil Object o) {
        if (o instanceof FiguraMat4 mat)
            return mat.times(this);
        else if (o instanceof FiguraVec4 vec)
            return this.times(vec);
        else if (o instanceof Number n)
            return this.copy().rawScale(n.doubleValue());

        throw new LuaError("Invalid types to Matrix4 __mul: " + o.getClass().getSimpleName());
    }
    @LuaWhitelist
    public boolean __eq(Object o) {
        return this.equals(o);
    }
    @LuaWhitelist
    public int __len() {
        return 4;
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
            case "4", "c4" -> this.getColumn(4);

            case "r1" -> this.getRow(1);
            case "r2" -> this.getRow(2);
            case "r3" -> this.getRow(3);
            case "r4" -> this.getRow(4);

            case "v11" -> this.v11;
            case "v12" -> this.v12;
            case "v13" -> this.v13;
            case "v14" -> this.v14;
            case "v21" -> this.v21;
            case "v22" -> this.v22;
            case "v23" -> this.v23;
            case "v24" -> this.v24;
            case "v31" -> this.v31;
            case "v32" -> this.v32;
            case "v33" -> this.v33;
            case "v34" -> this.v34;
            case "v41" -> this.v41;
            case "v42" -> this.v42;
            case "v43" -> this.v43;
            case "v44" -> this.v44;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String string, Object value) {
        if (value instanceof FiguraVec4 vec4) {
            switch (string) {
                case "1", "c1" -> {
                    v11 = vec4.x; v21 = vec4.y; v31 = vec4.z; v41 = vec4.w;
                }
                case "2", "c2" -> {
                    v12 = vec4.x; v22 = vec4.y; v32 = vec4.z; v42 = vec4.w;
                }
                case "3", "c3" -> {
                    v13 = vec4.x; v23 = vec4.y; v33 = vec4.z; v43 = vec4.w;
                }
                case "4", "c4" -> {
                    v14 = vec4.x; v24 = vec4.y; v34 = vec4.z; v44 = vec4.w;
                }
                case "r1" -> {
                    v11 = vec4.x; v12 = vec4.y; v13 = vec4.z; v14 = vec4.w;
                }
                case "r2" -> {
                    v21 = vec4.x; v22 = vec4.y; v23 = vec4.z; v24 = vec4.w;
                }
                case "r3" -> {
                    v31 = vec4.x; v32 = vec4.y; v33 = vec4.z; v34 = vec4.w;
                }
                case "r4" -> {
                    v41 = vec4.x; v42 = vec4.y; v43 = vec4.z; v44 = vec4.w;
                }
            }
            return;
        }
        if (value instanceof Number num) {
            switch (string) {
                case "v11" -> this.v11 = num.doubleValue();
                case "v12" -> this.v12 = num.doubleValue();
                case "v13" -> this.v13 = num.doubleValue();
                case "v14" -> this.v14 = num.doubleValue();
                case "v21" -> this.v21 = num.doubleValue();
                case "v22" -> this.v22 = num.doubleValue();
                case "v23" -> this.v23 = num.doubleValue();
                case "v24" -> this.v24 = num.doubleValue();
                case "v31" -> this.v31 = num.doubleValue();
                case "v32" -> this.v32 = num.doubleValue();
                case "v33" -> this.v33 = num.doubleValue();
                case "v34" -> this.v34 = num.doubleValue();
                case "v41" -> this.v41 = num.doubleValue();
                case "v42" -> this.v42 = num.doubleValue();
                case "v43" -> this.v43 = num.doubleValue();
                case "v44" -> this.v44 = num.doubleValue();
            }
            return;
        }
        throw new LuaError("Illegal arguments to Matrix4 __newindex: " + string + ", " + value);
    }
}
