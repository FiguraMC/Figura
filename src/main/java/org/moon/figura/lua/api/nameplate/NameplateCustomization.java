package org.moon.figura.lua.api.nameplate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.luaj.vm2.LuaError;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaType;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFunctionOverload;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.LuaUtils;
import org.moon.figura.utils.TextUtils;

@LuaType(typeName = "nameplate_customization")
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

        MutableComponent badges = Component.literal(" ").withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT).withColor(ChatFormatting.WHITE));

        // -- loading -- //

        if (!avatar.loaded) {
            badges.append(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16));
            return badges;
        }

        // -- mark -- //

        Pride[] pride = Pride.values();

        //error
        if (avatar.scriptError)
            badges.append(Default.ERROR.badge);

        //easter egg
        else if (FiguraMod.CHEESE_DAY && (boolean) Config.EASTER_EGGS.value)
            badges.append(Default.CHEESE.badge);

        //mark
        else if (avatar.nbt != null) {
            mark: {
                //pride (mark skins)
                for (int i = pride.length - 1; i >= 0; i--) {
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

        Special[] special = Special.values();

        //special badge
        for (int i = special.length - 1; i >= 0; i--) {
            if (avatar.badges.get(i + pride.length)) {
                badges.append(Component.literal(special[i].badge).withStyle(Style.EMPTY.withColor(special[i].color())));
                break;
            }
        }

        return badges.getString().isBlank() ? Component.empty() : badges;
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
        DONATOR("❤", ColorUtils.Colors.FRAN_PINK.hex),
        CONTEST("☆", ColorUtils.Colors.FRAN_PINK.hex),
        REDDIT_MOD("☆", ColorUtils.Colors.REDDIT_MOD.hex),
        DISCORD_MOD("☆", ColorUtils.Colors.DISCORD_MOD.hex),
        DISCORD_ADMIN("☆", ColorUtils.Colors.DISCORD_ADMIN.hex),
        DEV("★");

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
            return this.color == null ? 0xFFFFFF : this.color;
        }
    }

    public static int badgesLen() {
        return Pride.values().length + Special.values().length;
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
}
