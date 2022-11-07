package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

import java.util.BitSet;
import java.util.UUID;

public class Badges {

    private static final Pair<BitSet, BitSet> NO_BADGES = Pair.of(new BitSet(Pride.values().length), new BitSet(Special.values().length));

    public static Component fetchBadges(Avatar avatar) {
        if (avatar == null)
            return TextComponent.EMPTY.copy();

        MutableComponent badges = TextComponent.EMPTY.copy().withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT).withColor(ChatFormatting.WHITE));

        UUID id = avatar.owner;
        Pair<BitSet, BitSet> pair = UserData.getBadges(id);
        if (pair == null)
            pair = NO_BADGES;

        // -- loading -- //

        if (!avatar.loaded)
            return badges.append(new TextComponent(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)));

        // -- mark -- //

        if (avatar.nbt != null) {
            Pride[] pride = Pride.values();

            //error
            if (avatar.scriptError)
                badges.append(System.ERROR.badge);

            //version
            if (avatar.versionStatus > 0)
                badges.append(System.WARNING.badge);

            //egg
            if (FiguraMod.CHEESE_DAY && Config.EASTER_EGGS.asBool())
                badges.append(System.CHEESE.badge);

                //mark
            else {
                mark: {
                    //pride (mark skins)
                    BitSet prideSet = pair.getFirst();
                    for (int i = pride.length - 1; i >= 0; i--) {
                        if (prideSet.get(i)) {
                            badges.append(pride[i].badge);
                            break mark;
                        }
                    }

                    //mark fallback
                    badges.append(System.DEFAULT.badge.copy().withStyle(Style.EMPTY.withColor(ColorUtils.rgbToInt(ColorUtils.userInputHex(avatar.color)))));
                }
            }
        }

        // -- special -- //

        Special[] special = Special.values();

        //special badges
        BitSet specialSet = pair.getSecond();
        for (int i = special.length - 1; i >= 0; i--) {
            if (specialSet.get(i))
                badges.append(special[i].badge);
        }

        return badges;
    }

    public enum System {
        DEFAULT("△"),
        CHEESE("\uD83E\uDDC0"),
        WARNING("❗"),
        ERROR("❌");

        public final Component badge;
        public final Component desc;

        System(String unicode) {
            this.desc = new FiguraText("badges.system." + this.name().toLowerCase());
            this.badge = new TextComponent(unicode).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
        }
    }

    public enum Pride {
        AGENDER("ᚠ"),
        AROACE("ᚡ"),
        AROMANTIC("ᚢ"),
        ASEXUAL("ᚣ"),
        BIGENDER("ᚤ"),
        BISEXUAL("ᚥ"),
        DEMIBOY("ᚦ"),
        DEMIGENDER("ᚧ"),
        DEMIGIRL("ᚨ"),
        DEMIROMANTIC("ᚩ"),
        DEMISEXUAL("ᚪ"),
        DISABILITY("ᚫ"),
        FINSEXUAL("ᚬ"),
        GAYMEN("ᚭ"),
        GENDERFAE("ᚮ"),
        GENDERFLUID("ᚯ"),
        GENDERQUEER("ᚰ"),
        INTERSEX("ᚱ"),
        LESBIAN("ᚲ"),
        NONBINARY("ᚳ"),
        PANSEXUAL("ᚴ"),
        PLURAL("ᚵ"),
        POLYSEXUAL("ᚶ"),
        PRIDE("ᚷ"),
        TRANSGENDER("ᚸ");

        public final Component badge;
        public final Component desc;

        Pride(String unicode) {
            this.desc = new FiguraText("badges.pride." + this.name().toLowerCase());
            this.badge = new TextComponent(unicode).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
        }
    }

    public enum Special {
        DEV("★"),
        DISCORD_STAFF("☆", ColorUtils.Colors.DISCORD.hex),
        CONTEST("☆", ColorUtils.Colors.FRAN_PINK.hex),
        DONATOR("❤", ColorUtils.Colors.FRAN_PINK.hex),
        TRANSLATOR("☄"),

        SHADOW("\uD83C\uDF00"),
        MOON("\uD83C\uDF19"),
        SHRIMP("\uD83E\uDD90"),
        BURGER("\uD83C\uDF54");

        public final Component badge;
        public final Component desc;

        Special(String unicode) {
            this(unicode, null);
        }

        Special(String unicode, Integer color) {
            this.desc = new FiguraText("badges.special." + this.name().toLowerCase());
            Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc));
            if (color != null) style = style.withColor(color);
            this.badge = new TextComponent(unicode).withStyle(style);
        }
    }
}
