package org.moon.figura.lua.docs;

import org.moon.figura.math.vector.*;

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
                            argumentTypes = {FiguraVec2.class, FiguraVec2.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec2.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, FiguraVec3.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec3.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec4.class, FiguraVec4.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec4.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec5.class, FiguraVec5.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec5.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec6.class, FiguraVec6.class, Double.class},
                            argumentNames = {"a", "b", "t"},
                            returnType = FiguraVec6.class
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
