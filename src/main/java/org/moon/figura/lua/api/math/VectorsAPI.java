package org.moon.figura.lua.api.math;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.*;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "vectors",
        description = "A global API which provides functions dedicated " +
                "to creating and otherwise manipulating vectors."
)
public class VectorsAPI {

    public static final VectorsAPI INSTANCE = new VectorsAPI();

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"},
                            returnType = FiguraVec2.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraVec3.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"},
                            returnType = FiguraVec4.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"},
                            returnType = FiguraVec5.class
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t", "h"},
                            returnType = FiguraVec6.class
                    )
            },
            description = "Creates and returns a vector of the appropriate size to hold " +
                    "the arguments passed in. For example, if you call vec(3, 4, 0, 2), then " +
                    "the function will return a Vector4 containing those values."
    )
    public static Object vec(Double x, Double y, Double z, Double w, Double t, Double h) {
        if (h != null)
            return vec6(x, y, z, w, t, h);
        if (t != null)
            return vec5(x, y, z, w, t);
        if (w != null)
            return vec4(x, y, z, w);
        if (z != null)
            return vec3(x, y, z);
        if (y != null)
            return vec2(x, y);
        throw new LuaRuntimeException("Invalid arguments to vec(), needs at least 2 numbers!");
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"x", "y"},
                    returnType = FiguraVec2.class
            ),
            description = "Creates and returns a Vector2 with the given values. " +
                    "Nil values become zero."
    )
    public static FiguraVec2 vec2(Double x, Double y) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        return FiguraVec2.of(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z"},
                    returnType = FiguraVec3.class
            ),
            description = "Creates and returns a Vector3 with the given values. " +
                    "Nil values become zero."
    )
    public static FiguraVec3 vec3(Double x, Double y, Double z) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        return FiguraVec3.of(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z", "w"},
                    returnType = FiguraVec4.class
            ),
            description = "Creates and returns a Vector4 with the given values. " +
                    "Nil values become zero."
    )
    public static FiguraVec4 vec4(Double x, Double y, Double z, Double w) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        return FiguraVec4.of(x, y, z, w);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z", "w", "t"},
                    returnType = FiguraVec5.class
            ),
            description = "Creates and returns a Vector5 with the given values. " +
                    "Nil values become zero."
    )
    public static FiguraVec5 vec5(Double x, Double y, Double z, Double w, Double t) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        if (t == null) t = 0d;
        return FiguraVec5.of(x, y, z, w, t);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z", "w", "t", "h"},
                    returnType = FiguraVec6.class
            ),
            description = "Creates and returns a Vector6 with the given values. " +
                    "Nil values become zero."
    )
    public static FiguraVec6 vec6(Double x, Double y, Double z, Double w, Double t, Double h) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        if (t == null) t = 0d;
        if (h == null) h = 0d;
        return FiguraVec6.of(x, y, z, w, t, h);
    }

}
