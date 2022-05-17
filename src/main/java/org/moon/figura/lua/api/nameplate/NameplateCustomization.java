package org.moon.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.MathUtils;
import org.moon.figura.utils.TextUtils;
import org.terasology.jnlua.LuaRuntimeException;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateCustomization",
        description = "nameplate_customization"
)
public class NameplateCustomization {

    private static final String[] SPECIAL_BADGES = {"\uD83D\uDEAB", "\uD83C\uDF54", "❤", "☆", "✯", "\uD83C\uDF19", "★"};

    private String text;

    //those are only used on the ENTITY nameplate
    private FiguraVec3 position;
    private FiguraVec3 scale;
    private Boolean visible;

    public static Component applyCustomization(String text) {
        return TextUtils.removeClickableObjects(TextUtils.noBadges4U(TextUtils.tryParseJson(text)));
    }

    public static Component fetchBadges(Avatar avatar) {
        if (avatar == null)
            return TextComponent.EMPTY.copy();

        MutableComponent badges = new TextComponent(" ").withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));

        //error
        if (avatar.scriptError)
            badges.append("❌");

        //easter egg
        else if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value)
            badges.append("\uD83E\uDDC0");

        //pride
        else {
            int color = 0xFFFFFF;
            badges.append(new TextComponent(switch (avatar.badge.toLowerCase()) {
                case "lgbt", "pride", "gay" -> "\uD83D\uDFE5";
                case "transgender", "trans" -> "\uD83D\uDFE7";
                case "pansexual", "pan" -> "\uD83D\uDFE8";
                case "non binary", "non-binary", "nb", "enby" -> "\uD83D\uDFE9";
                case "plural", "plurality", "system" -> "\uD83D\uDFE6";
                case "bisexual", "bi" -> "\uD83D\uDFEA";
                case "asexual", "ace" -> "\uD83D\uDFEB";
                case "lesbian" -> "⬜";
                case "gender fluid", "genderfluid", "fluid" -> "⬛";
                default -> {
                    color = ColorUtils.userInputHex(avatar.badge);
                    yield "△";
                }
            }).withStyle(Style.EMPTY.withColor(color)));
        }

        //special badges
        long s = avatar.specialBadges;
        if (s > 0L) {
            for (int i = 0; i < 6; i++) {
                if (MathUtils.getBool(s, i))
                    badges.append(SPECIAL_BADGES[i]);
            }
            if (MathUtils.getBool(s, 6))
                badges.append(new TextComponent(SPECIAL_BADGES[6]).withStyle(Style.EMPTY.withColor(ColorUtils.rainbow(2))));
        }

        return badges;
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
