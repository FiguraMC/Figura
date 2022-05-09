package org.moon.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaFieldDoc;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.TextUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "NameplateCustomization",
        description = "nameplate_customization"
)
public class NameplateCustomization {

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_customization.text")
    public String text;

    //those are only used on the ENTITY nameplate
    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_customization.position")
    public FiguraVec3 position;

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_customization.scale")
    public FiguraVec3 scale;

    @LuaWhitelist
    @LuaFieldDoc(description = "nameplate_customization.visible")
    public Boolean visible;

    public static Component applyCustomization(String text) {
        text = TextUtils.noBadges4U(text);
        Component ret = TextUtils.tryParseJson(text);
        return TextUtils.removeClickableObjects(ret);
    }

    //TODO - fetch special badges from backend
    public static Component fetchBadges(Avatar avatar) {
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
                case "non binary", "non-binary", "nb" -> "⬛";
                case "bisexual", "bi" -> "\uD83D\uDFEA";
                case "asexual", "ace" -> "⬜";
                case "lesbian" -> "\uD83D\uDFE7";
                case "gender fluid", "genderfluid", "fluid" -> "\uD83D\uDFE9";
                default -> "△";
            };
        }

        //special
        //....

        return new TextComponent(ret).withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
    }

    @Override
    public String toString() {
        return "NameplateCustomization";
    }
}
