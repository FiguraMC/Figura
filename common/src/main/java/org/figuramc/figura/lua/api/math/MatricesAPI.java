package org.figuramc.figura.lua.api.math;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.matrix.FiguraMat2;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(
        name = "MatricesAPI",
        value = "matrices"
)
public class MatricesAPI {

    public static final MatricesAPI INSTANCE = new MatricesAPI();

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec2.class, FiguraVec2.class},
                            argumentNames = {"col1", "col2"}
                    )
            },
            value = "matrices.mat2"
    )
    public static FiguraMat2 mat2(FiguraVec2 col1, FiguraVec2 col2) {
        if (col1 == null && col2 == null)
            return FiguraMat2.of();
        if (col1 == null || col2 == null)
            throw new LuaError("Invalid arguments to mat2(), needs 0 or 2 arguments!");
        return FiguraMat2.of(
                col1.x, col1.y,
                col2.x, col2.y
        );
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"col1", "col2", "col3"}
                    )
            },
            value = "matrices.mat3"
    )
    public static FiguraMat3 mat3(FiguraVec3 col1, FiguraVec3 col2, FiguraVec3 col3) {
        if (col1 == null && col2 == null && col3 == null)
            return FiguraMat3.of();
        if (col1 == null || col2 == null || col3 == null)
            throw new LuaError("Invalid arguments to mat3(), needs 0 or 3 arguments!");
        return FiguraMat3.of(
                col1.x, col1.y, col1.z,
                col2.x, col2.y, col2.z,
                col3.x, col3.y, col3.z
        );
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload,
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec4.class, FiguraVec4.class, FiguraVec4.class, FiguraVec4.class},
                            argumentNames = {"col1", "col2", "col3", "col4"}
                    )
            },
            value = "matrices.mat4"
    )
    public static FiguraMat4 mat4(FiguraVec4 col1, FiguraVec4 col2, FiguraVec4 col3, FiguraVec4 col4) {
        if (col1 == null && col2 == null && col3 == null && col4 == null)
            return FiguraMat4.of();
        if (col1 == null || col2 == null || col3 == null || col4 == null)
            throw new LuaError("Invalid arguments to mat4(), needs 0 or 4 arguments!");
        return FiguraMat4.of(
                col1.x, col1.y, col1.z, col1.w,
                col2.x, col2.y, col2.z, col2.w,
                col3.x, col3.y, col3.z, col3.w,
                col4.x, col4.y, col4.z, col4.w
        );
    }

    // -- ROTATION MATRICES --// 
    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.rotation2"
    )
    public static FiguraMat2 rotation2(double degrees) {
        FiguraMat2 mat = FiguraMat2.of();
        mat.rotate(degrees);
        return mat;
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
            value = "matrices.rotation3"
    )
    public static FiguraMat3 rotation3(Object x, Double y, Double z) {
        FiguraVec3 angles = LuaUtils.parseVec3("rotation3", x, y, z);
        FiguraMat3 result = FiguraMat3.of();
        result.rotateZYX(angles);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.x_rotation3"
    )
    public static FiguraMat3 xRotation3(double degrees) {
        FiguraMat3 result = FiguraMat3.of();
        result.rotateX(degrees);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.y_rotation3"
    )
    public static FiguraMat3 yRotation3(double degrees) {
        FiguraMat3 result = FiguraMat3.of();
        result.rotateY(degrees);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.z_rotation3"
    )
    public static FiguraMat3 zRotation3(double degrees) {
        FiguraMat3 result = FiguraMat3.of();
        result.rotateZ(degrees);
        return result;
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
            value = "matrices.rotation4"
    )
    public static FiguraMat4 rotation4(Object x, Double y, Double z) {
        FiguraVec3 angles = LuaUtils.parseVec3("rotation4", x, y, z);
        FiguraMat4 result = FiguraMat4.of();
        result.rotateZYX(angles);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.x_rotation4"
    )
    public static FiguraMat4 xRotation4(double degrees) {
        FiguraMat4 result = FiguraMat4.of();
        result.rotateX(degrees);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.y_rotation4"
    )
    public static FiguraMat4 yRotation4(double degrees) {
        FiguraMat4 result = FiguraMat4.of();
        result.rotateY(degrees);
        return result;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "angle"
            ),
            value = "matrices.z_rotation4"
    )
    public static FiguraMat4 zRotation4(double degrees) {
        FiguraMat4 result = FiguraMat4.of();
        result.rotateZ(degrees);
        return result;
    }

    // -- SCALE MATRICES --// 
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
            value = "matrices.scale2"
    )
    public static FiguraMat2 scale2(Object x, Double y) {
        FiguraVec2 vec = LuaUtils.parseVec2("scale2", x, y, 1, 1);
        FiguraMat2 result = FiguraMat2.of();
        result.scale(vec);
        return result;
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
            value = "matrices.scale3"
    )
    public static FiguraMat3 scale3(Object x, Double y, Double z) {
        FiguraVec3 scale = LuaUtils.parseOneArgVec("scale3", x, y, z, 1d);
        FiguraMat3 result = FiguraMat3.of();
        result.scale(scale);
        return result;
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
            value = "matrices.scale4"
    )
    public static FiguraMat4 scale4(Object x, Double y, Double z) {
        FiguraVec3 scale = LuaUtils.parseOneArgVec("scale4", x, y, z, 1d);
        FiguraMat4 result = FiguraMat4.of();
        result.scale(scale);
        return result;
    }

    // -- TRANSLATION MATRICES --// 
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
            value = "matrices.translate3"
    )
    public static FiguraMat3 translate3(Object x, Double y) {
        FiguraVec2 offset = LuaUtils.parseVec2("translate3", x, y);
        FiguraMat3 result = FiguraMat3.of();
        result.translate(offset);
        return result;
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
            value = "matrices.translate4"
    )
    public static FiguraMat4 translate4(Object x, Double y, Double z) {
        FiguraVec3 offset = LuaUtils.parseVec3("translate4", x, y, z);
        FiguraMat4 result = FiguraMat4.of();
        result.translate(offset);
        return result;
    }

    @Override
    public String toString() {
        return "MatricesAPI";
    }
}
