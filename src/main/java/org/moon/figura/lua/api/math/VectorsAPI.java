package org.moon.figura.lua.api.math;

import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.*;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.MathUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "VectorsAPI",
        value = "vectors"
)
public class VectorsAPI {

    public static final VectorsAPI INSTANCE = new VectorsAPI();

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"x", "y"},
                            returnType = FiguraVec2.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"},
                            returnType = FiguraVec3.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w"},
                            returnType = FiguraVec4.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t"},
                            returnType = FiguraVec5.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z", "w", "t", "h"},
                            returnType = FiguraVec6.class
                    )
            },
            value = "vectors.vec"
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
        throw new LuaError("Invalid arguments to vec(), needs at least 2 numbers!");
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class},
                    argumentNames = {"x", "y"}
            ),
            value = "vectors.vec2"
    )
    public static FiguraVec2 vec2(double x, double y) {
        return FiguraVec2.of(x, y);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z"}
            ),
            value = "vectors.vec3"
    )
    public static FiguraVec3 vec3(double x, double y, double z) {
        return FiguraVec3.of(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z", "w"}
            ),
            value = "vectors.vec4"
    )
    public static FiguraVec4 vec4(double x, double y, double z, double w) {
        return FiguraVec4.of(x, y, z, w);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z", "w", "t"}
            ),
            value = "vectors.vec5"
    )
    public static FiguraVec5 vec5(double x, double y, double z, double w, double t) {
        return FiguraVec5.of(x, y, z, w, t);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                    argumentNames = {"x", "y", "z", "w", "t", "h"}
            ),
            value = "vectors.vec6"
    )
    public static FiguraVec6 vec6(double x, double y, double z, double w, double t, double h) {
        return FiguraVec6.of(x, y, z, w, t, h);
    }

    // -- colors -- //

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "vectors.rgb_to_int"
    )
    public static int rgbToInt(Object r, Double g, Double b) {
        FiguraVec3 rgb = LuaUtils.parseVec3("rgbToInt", r, g, b);
        return ColorUtils.rgbToInt(rgb);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Integer.class,
                    argumentNames = "color"
            ),
            value = "vectors.int_to_rgb"
    )
    public static FiguraVec3 intToRGB(int color) {
        return ColorUtils.intToRGB(color);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "hex"
            ),
            value = "vectors.hex_to_rgb"
    )
    public static FiguraVec3 hexToRGB(@LuaNotNil String hex) {
        return ColorUtils.intToRGB(ColorUtils.userInputHex(hex, FiguraVec3.of()));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "hsv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"h", "s", "v"}
                    )
            },
            value = "vectors.hsv_to_rgb"
    )
    public static FiguraVec3 hsvToRGB(Object h, Double s, Double v) {
        FiguraVec3 hsv = LuaUtils.parseVec3("hsvToRGB", h, s, v);
        return ColorUtils.hsvToRGB(hsv);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "vectors.rgb_to_hsv"
    )
    public static FiguraVec3 rgbToHSV(Object r, Double g, Double b) {
        FiguraVec3 rgb = LuaUtils.parseVec3("rgbToHSV", r, g, b);
        return ColorUtils.rgbToHSV(rgb);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            value = "vectors.rgb_to_hex"
    )
    public static String rgbToHex(Object r, Double g, Double b) {
        FiguraVec3 rgb = LuaUtils.parseVec3("rgbToHex", r, g, b);
        return ColorUtils.rgbToHex(rgb);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = Double.class,
                            argumentNames = "speed"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"speed", "offset"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"speed", "offset", "saturation", "light"}
                    )
            },
            value = "vectors.rainbow"
    )
    public static FiguraVec3 rainbow(Double speed, double offset, Double saturation, Double light) {
        if (speed == null) speed = 1d;
        if (saturation == null) saturation = 1d;
        if (light == null) light = 1d;
        return ColorUtils.rainbow(speed, offset, saturation, light);
    }

    // -- math utils -- //

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, FiguraVec3.class, FiguraVec3.class},
                            argumentNames = {"angle", "vec", "axis"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, FiguraVec3.class},
                            argumentNames = {"angle", "x", "y", "z", "axis"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, FiguraVec3.class, Double.class, Double.class, Double.class},
                            argumentNames = {"angle", "vec", "axisX", "axisY", "axisZ"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"angle", "x", "y", "z", "axisX", "axisY", "axisZ"}
                    )
            },
            value = "vectors.rotate_around_axis"
    )
    public static FiguraVec3 rotateAroundAxis(double angle, Object x, Object y, Double z, Object w, Double t, Double h) {
        FiguraVec3 vec, axis;

        //parse vec and axis (basically the same logic used in the particle#addParticle() method)
        if (x instanceof FiguraVec3 vec1) {
            vec = vec1.copy();
            if (y instanceof FiguraVec3 vec2) {
                axis = vec2.copy();
            } else if (y == null || y instanceof Number) {
                if (w == null || w instanceof Number) {
                    axis = LuaUtils.parseVec3("rotateAroundAxis", y, z, (Number) w);
                } else {
                    throw new LuaError("Illegal argument to rotateAroundAxis(): " + w);
                }
            } else {
                throw new LuaError("Illegal argument to rotateAroundAxis(): " + y);
            }
        } else if (x == null || x instanceof Number && y == null || y instanceof Number) {
            vec = LuaUtils.parseVec3("rotateAroundAxis", x, (Number) y, z);
            if (w instanceof FiguraVec3 vec1) {
                axis = vec1.copy();
            } else if (w == null || w instanceof Number) {
                axis = LuaUtils.parseVec3("rotateAroundAxis", w, t, h);
            } else {
                throw new LuaError("Illegal argument to rotateAroundAxis(): " + w);
            }
        } else {
            throw new LuaError("Illegal argument to rotateAroundAxis(): " + x);
        }

        FiguraVec3 result = MathUtils.rotateAroundAxis(vec, axis, angle);

        vec.free();
        axis.free();

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
            value = "vectors.to_camera_space"
    )
    public static FiguraVec3 toCameraSpace(Object x, Double y, Double z) {
        return MathUtils.toCameraSpace(LuaUtils.parseVec3("toCameraSpace", x, y, z));
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
            value = "vectors.world_to_screen_space"
    )
    public static FiguraVec4 worldToScreenSpace(Object x, Double y, Double z) {
        return MathUtils.worldToScreenSpace(LuaUtils.parseVec3("worldToScreenSpace", x, y, z));
    }

    @Override
    public String toString() {
        return "VectorsAPI";
    }
}
