package org.moon.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateCustomization",
        description = "nameplate_customization"
)
public class NameplateCustomization {

    private String text;

    //those are only used on the ENTITY nameplate
    private FiguraVec3 position;
    private FiguraVec3 scale;
    private Boolean visible;

    public static Component applyCustomization(String text) {
        return TextUtils.removeClickableObjects(TextUtils.noBadges4U(TextUtils.tryParseJson(text)));
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "nameplate_customization.get_text")
    public String getText() {
        return this.text;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = String.class,
                    argumentNames = "text"
            ),
            description = "nameplate_customization.set_text"
    )
    public void setText(@LuaNotNil String text) {
        if (TextUtils.tryParseJson(text).getString().length() > 256)
            throw new LuaError("Text length exceeded limit of 256 characters");
        this.text = text;
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "nameplate_customization.get_pos")
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
            description = "nameplate_customization.set_pos"
    )
    public void setPos(Object x, Double y, Double z) {
        this.position = x == null ? null : LuaUtils.parseVec3("setPosition", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "nameplate_customization.get_scale")
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
            description = "nameplate_customization.set_scale"
    )
    public void setScale(Object x, Double y, Double z) {
        this.scale = x == null ? null : LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
    }

    @LuaWhitelist
    @LuaMethodDoc(description = "nameplate_customization.is_visible")
    public Boolean isVisible() {
        return this.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            description = "nameplate_customization.set_visible"
    )
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "NameplateCustomization";
    }
}
