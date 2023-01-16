package org.moon.figura.lua.api.nameplate;

import net.minecraft.client.renderer.LightTexture;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaMethodShadow;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
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
    public Integer background, outlineColor, light;
    public Double alpha;

    public boolean visible = true;
    public boolean shadow, outline;

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
    public EntityNameplateCustomization setPos(Object x, Double y, Double z) {
        this.position = x == null ? null : LuaUtils.parseVec3("setPos", x, y, z);
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setPos")
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
            value = "nameplate_entity.set_scale"
    )
    public EntityNameplateCustomization setScale(Object x, Double y, Double z) {
        this.scale = x == null ? null : LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setScale")
    public EntityNameplateCustomization scale(Object x, Double y, Double z) {
        return setScale(x, y, z);
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
    public EntityNameplateCustomization setBackgroundColor(Object r, Double g, Double b, Double a) {
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
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setBackgroundColor")
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
            value = "nameplate_entity.set_outline_color"
    )
    public EntityNameplateCustomization setOutlineColor(Object x, Double y, Double z) {
        outlineColor = ColorUtils.rgbToInt(LuaUtils.parseVec3("setOutlineColor", x, y, z));
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOutlineColor")
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
    @LuaMethodShadow("setLight")
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
            value = "animation.set_visible"
    )
    public EntityNameplateCustomization setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setVisible")
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
            value = "animation.set_outline"
    )
    public EntityNameplateCustomization setOutline(boolean outline) {
        this.outline = outline;
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setOutline")
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
            value = "animation.set_shadow"
    )
    public EntityNameplateCustomization setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    @LuaWhitelist
    @LuaMethodShadow("setShadow")
    public EntityNameplateCustomization shadow(boolean shadow) {
        return setShadow(shadow);
    }

    @Override
    public String toString() {
        return "EntityNameplateCustomization";
    }
}
