package org.moon.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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
        return TextUtils.removeClickableObjects(TextUtils.noBadges4U(TextUtils.tryParseJson(text)));
    }

    public static Component fetchBadges(Avatar avatar) {
        if (avatar == null)
            return Component.empty();

        MutableComponent badges = Component.literal(" ").withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
        Pride[] pride = Pride.values();
        Special[] special = Special.values();

        // -- mark -- //

        //error
        if (avatar.scriptError)
            badges.append(Default.ERROR.badge);

        //easter egg
        else if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value)
            badges.append(Default.CHEESE.badge);

        //mark
        else {
            mark: {
                //pride (mark skins)
                for (int i = 0; i < pride.length; i++) {
                    if (avatar.badges.get(i)) {
                        badges.append(pride[i].badge);
                        break mark;
                    }
                }

                //mark fallback
                badges.append(Component.literal(Default.DEFAULT.badge).withStyle(Style.EMPTY.withColor(ColorUtils.userInputHex(avatar.color))));
            }
        }

        // -- special -- //

        //special badges
        for (int i = 0; i < special.length; i++) {
            if (avatar.badges.get(i + pride.length))
                badges.append(Component.literal(special[i].badge).withStyle(Style.EMPTY.withColor(special[i].color())));
        }

        return badges;
    }

    private enum Default {
        DEFAULT("△"),
        CHEESE("\uD83E\uDDC0"),
        WARNING("❗"),
        ERROR("❌");

        public final String badge;

        Default(String unicode) {
            this.badge = unicode;
        }
    }

    private enum Pride {
        PRIDE("\uD83D\uDFE5"),
        TRANS("\uD83D\uDFE7"),
        PAN("\uD83D\uDFE8"),
        ENBY("\uD83D\uDFE9"),
        PLURAL("\uD83D\uDFE6"),
        BI("\uD83D\uDFEA"),
        ACE("\uD83D\uDFEB"),
        LESBIAN("⬜"),
        FLUID("⬛");

        public final String badge;

        Pride(String unicode) {
            this.badge = unicode;
        }
    }

    private enum Special {
        BURGER("\uD83C\uDF54"),
        SHRIMP("\uD83E\uDD90"),
        MOON("\uD83C\uDF19"),
        SHADOW("\uD83C\uDF00"),
        BANNED("\uD83D\uDEAB"),
        DONATOR("❤", ColorUtils.Colors.FRAN_PINK.hex),
        CONTEST("★", ColorUtils.Colors.FRAN_PINK.hex),
        DISCORD("★", 0x7289DA),
        DEV("★", -1);

        public final String badge;
        private final Integer color;

        Special(String unicode) {
            this(unicode, null);
        }

        Special(String unicode, Integer color) {
            this.badge = unicode;
            this.color = color;
        }

        public int color() {
            return this.color == null ? 0xFFFFFF : this.color == -1 ? ColorUtils.rgbToInt(ColorUtils.rainbow(2d, 0.7d, 1d)) : this.color;
        }
    }

    public static int badgesLen() {
        return Pride.values().length + Special.values().length;
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
                            argumentNames = {"customization", "pos"}
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
                            argumentNames = {"customization", "scale"}
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
