package org.moon.figura.newlua.docs;

import org.moon.figura.math.vector.FiguraVector;

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
                            argumentTypes = {FiguraVector.class, FiguraVector.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVector.class
                    )
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
