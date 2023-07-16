package org.figuramc.figura.lua.docs;

import org.figuramc.figura.math.matrix.FiguraMatrix;
import org.figuramc.figura.math.vector.FiguraVector;

/**
 * Adds docs for the functions added to Lua's math library.
 */
@LuaTypeDoc(
        name = "math",
        value = "math"
)
public class FiguraMathDocs {

    @LuaFieldDoc("math.player_scale")
    public double playerScale;
    @LuaFieldDoc("math.world_scale")
    public double worldScale;

    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = Double.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVector.class, FiguraVector.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVector.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraMatrix.class, FiguraMatrix.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraMatrix.class
                    ),
            },
            value = "math.lerp"
    )
    public static Object lerp() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class},
                    argumentNames = {"value", "min", "max"}
            ),
            value = "math.clamp"
    )
    public static Double clamp() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "value"
            ),
            value = "math.round"
    )
    public static Double round() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"value", "oldMin", "oldMax", "newMin", "newMax"}
            ),
            value = "math.map"
    )
    public static Double map() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"from", "to"}
            ),
            value = "math.short_angle"
    )
    public static Double shortAngle() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class},
                    argumentNames = {"a", "b", "t"}
            ),
            value = "math.lerp_angle"
    )
    public static Double lerpAngle() {return null;}

    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Double.class,
                    argumentNames = "value"
            ),
            value = "math.sign"
    )
    public static Double sign() {return null;}
}
