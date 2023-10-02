package org.figuramc.figura.lua.api.math;

import com.mojang.datafixers.util.Pair;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;
import org.figuramc.figura.utils.MathUtils;
import org.luaj.vm2.LuaError;

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
                    )
            },
            value = "vectors.vec"
    )
    public static Object vec(Double x, Double y, Double z, Double w) {
        if (x == null)
            throw new LuaError("Illegal argument to vec(): x, " + x);

        if (w != null) {
            if (y == null)
                throw new LuaError("Illegal argument to vec(): y," + y);
            if (z == null)
                throw new LuaError("Illegal argument to vec(): z," + z);
            return vec4(x, y, z, w);
        }
        if (z != null){
            if (y == null)
                throw new LuaError("Illegal argument to vec(): y," + y);
            return vec3(x, y, z);
        }
        if (y != null) {
            return vec2(x, y);
        }
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
        return ColorUtils.userInputHex(hex, FiguraVec3.of());
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

        Pair<FiguraVec3, FiguraVec3> pair = LuaUtils.parse2Vec3("rotateAroundAxis", x, y, z, w, t, h, 2);
        vec = pair.getFirst();
        axis = pair.getSecond();

        return MathUtils.rotateAroundAxis(vec, axis, angle);
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

    @LuaWhitelist
    @LuaMethodDoc(overloads = {
            @LuaMethodOverload(argumentTypes = FiguraVec2.class, argumentNames = "vec"),
            @LuaMethodOverload(argumentTypes = {Double.class, Double.class}, argumentNames = {"pitch", "yaw"})
    }, value = "vectors.angle_to_dir")
    public static FiguraVec3 angleToDir(Object pitch, double yaw) {
        FiguraVec2 vec = LuaUtils.parseVec2("angleToDir", pitch, yaw);
        double radPitch = Math.toRadians(vec.x);
        double radYaw = Math.toRadians(-vec.y);
        double cos = Math.cos(radPitch);
        return FiguraVec3.of(Math.sin(radYaw) * cos, -Math.sin(radPitch), Math.cos(radYaw) * cos);
    }

    @Override
    public String toString() {
        return "VectorsAPI";
    }
}
