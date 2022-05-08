package org.moon.figura.lua.api.nameplate;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.utils.TextUtils;

@LuaWhitelist
public class NameplateCustomization {

    @LuaWhitelist
    public String text;

    //those are only used on the ENTITY nameplate
    @LuaWhitelist
    public FiguraVec3 position;

    @LuaWhitelist
    public FiguraVec3 scale;

    @LuaWhitelist
    public Boolean enabled;

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
            ret += switch (avatar.pride) {
                case "trans" -> "⚧";
                case "gay" -> "\uD83C\uDF08";
                default -> "△";
            };
        }

        //special
        //....

        return new TextComponent(ret).withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT));
    }
}
