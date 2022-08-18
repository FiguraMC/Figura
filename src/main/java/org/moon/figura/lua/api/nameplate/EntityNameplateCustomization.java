package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "EntityNameplateCustomization",
        description = "nameplate_entity"
)
public class EntityNameplateCustomization extends NameplateCustomization {

    private FiguraVec3 position;
    private FiguraVec3 scale;

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_entity.visible")
    public boolean visible = true;
    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_entity.background")
    public boolean background = true;
    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_entity.shadow")
    public boolean shadow;
    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_entity.outline")
    public boolean outline;

    @LuaWhitelist
    @LuaMethodDoc(description = "nameplate_entity.get_pos")
    public FiguraVec3 getPos() {
        return this.position;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pos"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "nameplate_entity.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        this.position = x == null ? null : LuaUtils.parseVec3("setPosition", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "nameplate_entity.get_scale")
    public FiguraVec3 getScale() {
        return this.scale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "scale"
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            description = "nameplate_entity.set_scale"
    )
    public void setScale(Object x, Double y, Double z) {
        this.scale = x == null ? null : LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        if (arg == null) return null;
        return switch (arg) {
            case "visible" -> visible;
            case "background" -> background;
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
            case "background" -> background = value;
            case "shadow" -> shadow = value;
            case "outline" -> outline = value;
        }
    }

    @Override
    public String toString() {
        return "EntityNameplateCustomization";
    }
}
