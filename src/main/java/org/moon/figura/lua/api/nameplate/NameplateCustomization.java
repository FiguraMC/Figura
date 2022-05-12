package org.moon.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;
import org.terasology.jnlua.LuaRuntimeException;

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
        text = TextUtils.noBadges4U(text);
        Component ret = TextUtils.tryParseJson(text);
        return TextUtils.removeClickableObjects(ret);
    }

    public static Component fetchBadges(Avatar avatar) {
        if (avatar == null)
            return TextComponent.EMPTY.copy();

        String ret = " ";

        //error
        if (avatar.scriptError)
            ret += "▲";

        //easter egg
        else if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value)
            ret += "\uD83E\uDDC0";

        //pride
        else {
            ret += switch (avatar.pride.toLowerCase()) {
                case "lgbt", "pride", "gay" -> "\uD83D\uDFE5";
                case "transgender", "trans" -> "\uD83D\uDFE6";
                case "pansexual", "pan" -> "\uD83D\uDFE8";
                case "non binary", "non-binary", "nb", "enby" -> "⬛";
                case "bisexual", "bi" -> "\uD83D\uDFEA";
                case "asexual", "ace" -> "⬜";
                case "lesbian" -> "\uD83D\uDFE7";
                case "gender fluid", "genderfluid", "fluid" -> "\uD83D\uDFE9";
                default -> "△";
            };
        }

        //special
        ret += avatar.badges;

        return new TextComponent(ret).withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = NameplateCustomization.class,
                    argumentNames = "customization"
            ),
            description = "nameplate_customization.get_text"
    )
    public static String getText(@LuaNotNil NameplateCustomization custom) {
        return custom.text;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {NameplateCustomization.class, String.class},
                    argumentNames = {"customization", "text"}
            ),
            description = "nameplate_customization.set_text"
    )
    public static void setText(@LuaNotNil NameplateCustomization custom, @LuaNotNil String text) {
        if (TextUtils.tryParseJson(text).getString().length() > 256)
            throw new LuaRuntimeException("Text length exceeded limit of 256 characters");
        custom.text = text;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = NameplateCustomization.class,
                    argumentNames = "customization"
            ),
            description = "nameplate_customization.get_pos"
    )
    public static FiguraVec3 getPos(@LuaNotNil NameplateCustomization custom) {
        return custom.position;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {NameplateCustomization.class, FiguraVec3.class},
                            argumentNames = {"customization", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {NameplateCustomization.class, Double.class, Double.class, Double.class},
                            argumentNames = {"customization", "x", "y", "z"}
                    )
            },
            description = "nameplate_customization.set_pos"
    )
    public static void setPos(@LuaNotNil NameplateCustomization custom, Object x, Double y, Double z) {
        custom.position = LuaUtils.parseVec3("setPosition", x, y, z);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = NameplateCustomization.class,
                    argumentNames = "customization"
            ),
            description = "nameplate_customization.get_scale"
    )
    public static FiguraVec3 getScale(@LuaNotNil NameplateCustomization custom) {
        return custom.scale;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaFunctionOverload(
                            argumentTypes = {NameplateCustomization.class, FiguraVec3.class},
                            argumentNames = {"customization", "color"}
                    ),
                    @LuaFunctionOverload(
                            argumentTypes = {NameplateCustomization.class, Double.class, Double.class, Double.class},
                            argumentNames = {"customization", "x", "y", "z"}
                    )
            },
            description = "nameplate_customization.set_scale"
    )
    public static void setScale(@LuaNotNil NameplateCustomization custom, Object x, Double y, Double z) {
        custom.scale = LuaUtils.parseVec3("setScale", x, y, z, 1, 1, 1);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = NameplateCustomization.class,
                    argumentNames = "customization"
            ),
            description = "nameplate_customization.is_visible"
    )
    public static Boolean isVisible(@LuaNotNil NameplateCustomization custom) {
        return custom.visible;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaFunctionOverload(
                    argumentTypes = {NameplateCustomization.class, Boolean.class},
                    argumentNames = {"customization", "visible"}
            ),
            description = "nameplate_customization.set_visible"
    )
    public static void setVisible(@LuaNotNil NameplateCustomization custom, Boolean visible) {
        custom.visible = visible;
    }

    @Override
    public String toString() {
        return "NameplateCustomization";
    }
}
