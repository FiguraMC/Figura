package org.moon.figura.lua.api.math;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.math.vector.*;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
public class VectorsAPI {

    public static final VectorsAPI INSTANCE = new VectorsAPI();

    @LuaWhitelist
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
    public static FiguraVec2 vec2(Double x, Double y) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        return FiguraVec2.of(x, y);
    }

    @LuaWhitelist
    public static FiguraVec3 vec3(Double x, Double y, Double z) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        return FiguraVec3.of(x, y, z);
    }

    @LuaWhitelist
    public static FiguraVec4 vec4(Double x, Double y, Double z, Double w) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        return FiguraVec4.of(x, y, z, w);
    }

    @LuaWhitelist
    public static FiguraVec5 vec5(Double x, Double y, Double z, Double w, Double t) {
        if (x == null) x = 0d;
        if (y == null) y = 0d;
        if (z == null) z = 0d;
        if (w == null) w = 0d;
        if (t == null) t = 0d;
        return FiguraVec5.of(x, y, z, w, t);
    }

    @LuaWhitelist
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
