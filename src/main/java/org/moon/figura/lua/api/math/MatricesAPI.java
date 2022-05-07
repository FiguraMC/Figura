package org.moon.figura.lua.api.math;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.utils.LuaUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "MatricesAPI",
        description = "A global API which provides functions dedicated " +
                "to creating and otherwise manipulating matrices. Accessed using the name \"matrices\"."
)
public class MatricesAPI {

    public static final MatricesAPI INSTANCE = new MatricesAPI();

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = FiguraMat2.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec2.class, FiguraVec2.class},
                            argumentNames = {"col1", "col2"},
                            returnType = FiguraMat2.class
                    )
            },
            description = "Creates a Matrix2 using the given parameters as columns. " +
                    "If you call the function with no parameters, returns the 2x2 identity matrix."
    )
    public static FiguraMat2 mat2(FiguraVec2 col1, FiguraVec2 col2) {
        if (col1 == null && col2 == null)
            return FiguraMat2.of();
        if (col1 == null || col2 == null)
            throw new LuaRuntimeException("Invalid arguments to mat2(), needs 0 or 2 arguments!");
        return FiguraMat2.of(
                col1.x, col1.y,
                col2.x, col2.y
        );
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = FiguraMat3.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"col1", "col2", "col3"},
                            returnType = FiguraMat3.class
                    )
            },
            description = "Creates a Matrix3 using the given parameters as columns. " +
                    "If you call the function with no parameters, returns the 3x3 identity matrix."
    )
    public static FiguraMat3 mat3(FiguraVec3 col1, FiguraVec3 col2, FiguraVec3 col3) {
        if (col1 == null && col2 == null && col3 == null)
            return FiguraMat3.of();
        if (col1 == null || col2 == null || col3 == null)
            throw new LuaRuntimeException("Invalid arguments to mat3(), needs 0 or 3 arguments!");
        return FiguraMat3.of(
                col1.x, col1.y, col1.z,
                col2.x, col2.y, col2.z,
                col3.x, col3.y, col3.z
        );
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {},
                            argumentNames = {},
                            returnType = FiguraMat4.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class, FiguraVec4.class},
                            argumentNames = {"col1", "col2", "col3", "col4"},
                            returnType = FiguraMat4.class
                    )
            },
            description = "Creates a Matrix4 using the given parameters as columns. " +
                    "If you call the function with no parameters, returns the 4x4 identity matrix."
    )
    public static FiguraMat4 mat4(FiguraVec4 col1, FiguraVec4 col2, FiguraVec4 col3, FiguraVec4 col4) {
        if (col1 == null && col2 == null && col3 == null && col4 == null)
            return FiguraMat4.of();
        if (col1 == null || col2 == null || col3 == null || col4 == null)
            throw new LuaRuntimeException("Invalid arguments to mat4(), needs 0 or 4 arguments!");
        return FiguraMat4.of(
                col1.x, col1.y, col1.z, col1.w,
                col2.x, col2.y, col2.z, col2.w,
                col3.x, col3.y, col3.z, col3.w,
                col4.x, col4.y, col4.z, col4.w
        );
    }

    //-- ROTATION MATRICES --//
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat2.class
            ),
            description = "Creates a new Matrix2 that rotates by the specified angle. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat2 rotation2(Double degrees) {
        LuaUtils.nullCheck("rotation2", "degrees", degrees);
        return FiguraMat2.createRotationMatrix(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec",
                            returnType = FiguraMat3.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraMat3.class
                    )
            },
            description = "Creates a new Matrix3 that rotates by the specified angles. " +
                    "Angles are given in degrees, and the rotation order is ZYX."
    )
    public static FiguraMat3 rotation3(Object x, Double y, Double z) {
        if (x instanceof FiguraVec3 angles)
            return FiguraMat3.createZYXRotationMatrix(angles.x, angles.y, angles.z);
        else if (x == null || x instanceof Double) {
            if (x == null) x = 0d;
            if (y == null) y = 0d;
            if (z == null) z = 0d;
            return FiguraMat3.createZYXRotationMatrix((double) x, y, z);
        } else {
            throw new LuaRuntimeException("Illegal argument to rotation3(): " + x);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat3.class
            ),
            description = "Creates a new Matrix3 that rotates by the specified angle around the X axis. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat3 xRotation3(Double degrees) {
        LuaUtils.nullCheck("xRotation3", "degrees", degrees);
        return FiguraMat3.createXRotationMatrix(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat3.class
            ),
            description = "Creates a new Matrix3 that rotates by the specified angle around the Y axis. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat3 yRotation3(Double degrees) {
        LuaUtils.nullCheck("yRotation3", "degrees", degrees);
        return FiguraMat3.createYRotationMatrix(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat3.class
            ),
            description = "Creates a new Matrix3 that rotates by the specified angle around the Z axis. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat3 zRotation3(Double degrees) {
        LuaUtils.nullCheck("zRotation3", "degrees", degrees);
        return FiguraMat3.createZRotationMatrix(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec",
                            returnType = FiguraMat4.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraMat4.class
                    )
            },
            description = "Creates a new Matrix4 that rotates by the specified angles. " +
                    "Angles are given in degrees, and the rotation order is ZYX."
    )
    public static FiguraMat4 rotation4(Object x, Double y, Double z) {
        if (x instanceof FiguraVec3 angles)
            return FiguraMat4.createZYXRotationMatrix(angles.x, angles.y, angles.z);
        else if (x == null || x instanceof Double) {
            if (x == null) x = 0d;
            if (y == null) y = 0d;
            if (z == null) z = 0d;
            return FiguraMat4.createZYXRotationMatrix((double) x, y, z);
        } else {
            throw new LuaRuntimeException("Illegal argument to rotation4(): " + x);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat4.class
            ),
            description = "Creates a new Matrix4 that rotates by the specified angle around the X axis. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat4 xRotation4(Double degrees) {
        LuaUtils.nullCheck("xRotation4", "degrees", degrees);
        return FiguraMat4.createXRotationMatrix(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat4.class
            ),
            description = "Creates a new Matrix4 that rotates by the specified angle around the Y axis. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat4 yRotation4(Double degrees) {
        LuaUtils.nullCheck("yRotation4", "degrees", degrees);
        return FiguraMat4.createYRotationMatrix(degrees);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle",
                    returnType = FiguraMat4.class
            ),
            description = "Creates a new Matrix4 that rotates by the specified angle around the Z axis. " +
                    "Angle is given in degrees."
    )
    public static FiguraMat4 zRotation4(Double degrees) {
        LuaUtils.nullCheck("zRotation4", "degrees", degrees);
        return FiguraMat4.createZRotationMatrix(degrees);
    }

    //-- SCALE MATRICES --//
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "vec",
                            returnType = FiguraMat2.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"},
                            returnType = FiguraMat2.class
                    )
            },
            description = "Creates a new Matrix2 that scales by the specified factors."
    )
    public static FiguraMat2 scale2(Object x, Double y) {
        if (x instanceof FiguraVec2 vec)
            return FiguraMat2.createScaleMatrix(vec.x, vec.y);
        else if (x == null || x instanceof Double) {
            if (x == null) x = 1d;
            if (y == null) y = 1d;
            return FiguraMat2.createScaleMatrix((double) x, y);
        } else {
            throw new LuaRuntimeException("Illegal argument to scale2(): " + x);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec",
                            returnType = FiguraMat3.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraMat3.class
                    )
            },
            description = "Creates a new Matrix3 that scales by the specified factors."
    )
    public static FiguraMat3 scale3(Object x, Double y, Double z) {
        if (x instanceof FiguraVec3 vec)
            return FiguraMat3.createScaleMatrix(vec.x, vec.y, vec.z);
        else if (x == null || x instanceof Double) {
            if (x == null) x = 1d;
            if (y == null) y = 1d;
            if (z == null) z = 1d;
            return FiguraMat3.createScaleMatrix((double) x, y, z);
        } else {
            throw new LuaRuntimeException("Illegal argument to scale3(): " + x);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec",
                            returnType = FiguraMat4.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraMat4.class
                    )
            },
            description = "Creates a new Matrix4 that scales by the specified factors."
    )
    public static FiguraMat4 scale4(Object x, Double y, Double z) {
        if (x instanceof FiguraVec3 vec)
            return FiguraMat4.createScaleMatrix(vec.x, vec.y, vec.z);
        else if (x == null || x instanceof Double) {
            if (x == null) x = 1d;
            if (y == null) y = 1d;
            if (z == null) z = 1d;
            return FiguraMat4.createScaleMatrix((double) x, y, z);
        } else {
            throw new LuaRuntimeException("Illegal argument to scale4(): " + x);
        }
    }

    //-- TRANSLATION MATRICES --//
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "vec",
                            returnType = FiguraMat4.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraMat4.class
                    )
            },
            description = "Creates a new Matrix4 that translates by the specified offset."
    )
    public static FiguraMat4 translate4(Object x, Double y, Double z) {
        if (x instanceof FiguraVec3 vec)
            return FiguraMat4.createTranslationMatrix(vec.x, vec.y, vec.z);
        else if (x == null || x instanceof Double) {
            if (x == null) x = 0d;
            if (y == null) y = 0d;
            if (z == null) z = 0d;
            return FiguraMat4.createTranslationMatrix((double) x, y, z);
        } else {
            throw new LuaRuntimeException("Illegal argument to translate4(): " + x);
        }
    }

}
