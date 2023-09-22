package org.figuramc.figura.lua.api.nameplate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "EntityNameplateCustomization",
        value = "nameplate_entity"
)
public class EntityNameplateCustomization extends NameplateCustomization {

    private FiguraVec3 pivot, position, scale;
    public Integer background, outlineColor, light;

    public boolean visible = true;
    public boolean shadow, outline;

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.get_pivot")
    public FiguraVec3 getPivot() {
        return this.pivot;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "pivot"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"x", "y", "z"}
                    )
            },
            aliases = "pivot",
            value = "nameplate_entity.set_pivot"
    )
    public EntityNameplateCustomization setPivot(Object x, Double y, Double z) {
        this.pivot = LuaUtils.nullableVec3("setPivot", x, y, z);
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization pivot(Object x, Double y, Double z) {
        return setPivot(x, y, z);
    }

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
            aliases = "pos",
            value = "nameplate_entity.set_pos"
    )
    public EntityNameplateCustomization setPos(Object x, Double y, Double z) {
        this.position = LuaUtils.nullableVec3("setPos", x, y, z);
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization pos(Object x, Double y, Double z) {
        return setPos(x, y, z);
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
            aliases = "scale",
            value = "nameplate_entity.set_scale"
    )
    public EntityNameplateCustomization setScale(Object x, Double y, Double z) {
        this.scale = x == null ? null : LuaUtils.parseOneArgVec("setScale", x, y, z, 1d);
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization scale(Object x, Double y, Double z) {
        return setScale(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.get_background_color")
    public FiguraVec4 getBackgroundColor() {
        return this.background == null ? null : ColorUtils.intToRGBA(this.background);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "rgba"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b", "a"}
                    )
            },
            aliases = "backgroundColor",
            value = "nameplate_entity.set_background_color"
    )
    public EntityNameplateCustomization setBackgroundColor(Object r, Double g, Double b, Double a) {
        FiguraVec4 vec = LuaUtils.parseVec4("setBackgroundColor", r, g, b, a, 0, 0, 0,  Minecraft.getInstance().options.getBackgroundOpacity(0.25f));
        this.background = ColorUtils.rgbaToInt(vec);
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization backgroundColor(Object r, Double g, Double b, Double a) {
        return setBackgroundColor(r, g, b, a);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "color"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b"}
                    )
            },
            aliases = "outlineColor",
            value = "nameplate_entity.set_outline_color"
    )
    public EntityNameplateCustomization setOutlineColor(Object x, Double y, Double z) {
        outlineColor = ColorUtils.rgbToInt(LuaUtils.parseVec3("setOutlineColor", x, y, z));
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization outlineColor(Object x, Double y, Double z) {
        return setOutlineColor(x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.get_light")
    public FiguraVec2 getLight() {
        return light == null ? null : FiguraVec2.of(LightTexture.block(light), LightTexture.sky(light));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "light"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"blockLight", "skyLight"}
                    )
            },
            aliases = "light",
            value = "nameplate_entity.set_light")
    public EntityNameplateCustomization setLight(Object light, Double skyLight) {
        if (light == null) {
            this.light = null;
            return this;
        }

        FiguraVec2 lightVec = LuaUtils.parseVec2("setLight", light, skyLight);
        this.light = LightTexture.pack((int) lightVec.x, (int) lightVec.y);
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization light(Object light, Double skyLight) {
        return setLight(light, skyLight);
    }

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.is_visible")
    public boolean isVisible() {
        return visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "visible"
            ),
            aliases = "visible",
            value = "nameplate_entity.set_visible"
    )
    public EntityNameplateCustomization setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization visible(boolean visible) {
        return setVisible(visible);
    }

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.has_outline")
    public boolean hasOutline() {
        return outline;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "outline"
            ),
            aliases = "outline",
            value = "nameplate_entity.set_outline"
    )
    public EntityNameplateCustomization setOutline(boolean outline) {
        this.outline = outline;
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization outline(boolean outline) {
        return setOutline(outline);
    }

    @LuaWhitelist
    @LuaMethodDoc("nameplate_entity.has_shadow")
    public boolean hasShadow() {
        return shadow;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = Boolean.class,
                    argumentNames = "shadow"
            ),
            aliases = "shadow",
            value = "nameplate_entity.set_shadow"
    )
    public EntityNameplateCustomization setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @LuaWhitelist
    public EntityNameplateCustomization shadow(boolean shadow) {
        return setShadow(shadow);
    }

    @Override
    public String toString() {
        return "EntityNameplateCustomization";
    }
}
