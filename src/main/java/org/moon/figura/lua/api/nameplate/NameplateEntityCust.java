package org.moon.figura.lua.api.nameplate;

import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateEntityCust",
        description = "nameplate_entity"
)
public class NameplateEntityCust extends NameplateCustomization {

    private FiguraVec3 position;
    private FiguraVec3 scale;
    private Boolean visible;

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
    @LuaMethodDoc(description = "nameplate_entity.is_visible")
    public Boolean isVisible() {
        return this.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            description = "nameplate_entity.set_visible"
    )
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "NameplateEntityCust";
    }
}
