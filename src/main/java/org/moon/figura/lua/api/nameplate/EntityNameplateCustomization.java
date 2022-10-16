package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "EntityNameplateCustomization",
        value = "nameplate_entity"
)
public class EntityNameplateCustomization extends NameplateCustomization {

    private FiguraVec3 position;
    private FiguraVec3 scale;
    public Integer background;
    public Double alpha;

    @LuaWhitelist
    @LuaFieldDoc("nameplate_entity.visible")
    public boolean visible = true;
    @LuaWhitelist
    @LuaFieldDoc("nameplate_entity.shadow")
    public boolean shadow;
    @LuaWhitelist
    @LuaFieldDoc("nameplate_entity.outline")
    public boolean outline;

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.get_pos")
    public FiguraVec3 getPos() {
        return this.position;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "nameplate_entity.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        this.position = x == null ? null : LuaUtils.parseVec3("setPos", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.get_scale")
    public FiguraVec3 getScale() {
        return this.scale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            value = "nameplate_entity.set_scale"
    )
    public void setScale(Object x, Double y, Double z) {
        this.scale = x == null ? null : LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "rgba"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraVec3.class, Double.class},
                            argumentNames = {"rgb", "a"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b", "a"}
                    )
            },
            value = "nameplate_entity.set_background_color"
    )
    public void setBackgroundColor(Object r, Double g, Double b, Double a) {
        if (r == null) {
            this.background = null;
            this.alpha = null;
        } else if (r instanceof FiguraVec3 vec) {
            this.background = ColorUtils.rgbToInt(vec);
            this.alpha = g;
        } else if (r instanceof FiguraVec4 vec) {
            this.background = ColorUtils.rgbToInt(FiguraVec3.of(vec.x, vec.y, vec.z));
            this.alpha = vec.w;
        } else {
            FiguraVec3 vec = LuaUtils.parseVec3("setBackgroundColor", r, g, b);
            this.background = ColorUtils.rgbToInt(vec);
            this.alpha = a;
        }
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "visible" -> visible;
            case "shadow" -> shadow;
            case "outline" -> outline;
            default -> null;
        };
    }

    @LuaWhitelist
    public void __newindex(String key, boolean value) {
        if (key == null) return;
        switch (key) {
            case "visible" -> visible = value;
            case "shadow" -> shadow = value;
            case "outline" -> outline = value;
        }
    }

    @Override
    public String toString() {
        return "EntityNameplateCustomization";
    }
}
