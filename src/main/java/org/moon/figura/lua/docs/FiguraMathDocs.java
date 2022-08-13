package org.moon.figura.lua.docs;

import org.moon.figura.math.vector.*;

/**
 * Adds docs for the functions added to Lua's math library.
 */
@LuaTypeDoc(
        name = "math",
        description = "math"
)
public class FiguraMathDocs {
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = Double.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec2.class, FiguraVec2.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec2.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec3.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec4.class, FiguraVec4.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec4.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec5.class, FiguraVec5.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec5.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {FiguraVec6.class, FiguraVec6.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec6.class
                    ),
            },
            description = "math.lerp"
    )
    public static Object lerp() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class},
                    argumentNames = {"value", "min", "max"}
            ),
            description = "math.clamp"
    )
    public static Double clamp() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "value"
            ),
            description = "math.round"
    )
    public static Double round() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"value", "oldMin", "oldMax", "newMin", "newMax"}
            ),
            description = "math.map"
    )
    public static Double map() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"from", "to"}
            ),
            description = "math.short_angle"
    )
    public static Double shortAngle() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class},
                    argumentNames = {"a", "b", "t"}
            ),
            description = "math.lerp_angle"
    )
    public static Double lerpAngle() {return null;}

    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Double.class,
                    argumentNames = "value"
            ),
            description = "math.sign"
    )
    public static Double sign() {return null;}
}
