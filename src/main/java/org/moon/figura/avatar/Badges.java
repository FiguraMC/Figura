package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

import java.util.BitSet;
import java.util.UUID;

public class Badges {

    private static final Pair<BitSet, BitSet> NO_BADGES = Pair.of(new BitSet(Pride.values().length), new BitSet(Special.values().length));
    public static final ResourceLocation FONT = new FiguraIdentifier("badges");

    public static Component fetchBadges(UUID id) {
        MutableComponent badges = Component.empty().withStyle(Style.EMPTY.withFont(FONT).withColor(ChatFormatting.WHITE));

        //get user data
        Pair<BitSet, BitSet> pair = AvatarManager.getBadges(id);
        if (pair == null)
            pair = NO_BADGES;

        //avatar badges
        Avatar avatar = AvatarManager.getAvatarForPlayer(id);
        if (avatar != null) {

            // -- loading -- //

            if (!avatar.loaded)
                badges.append(Component.literal(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)));

            // -- mark -- //

            else if (avatar.nbt != null) {
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

    public static Component noBadges4U(Component text) {
        return TextUtils.replaceInText(text, ".*", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(FONT));
    }

    public enum System {
        DEFAULT("△"),
        CHEESE("\uD83E\uDDC0"),
        WARNING("❗"),
        ERROR("❌");

        public final Component badge;
        public final Component desc;

        System(String unicode) {
            this.desc = FiguraText.of("badges.system." + this.name().toLowerCase());
            this.badge = Component.literal(unicode).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
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
            this.desc = FiguraText.of("badges.pride." + this.name().toLowerCase());
            this.badge = Component.literal(unicode).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc)));
        }
    }

    public enum Special {
        DEV("★"),
        DISCORD_STAFF("☆", ColorUtils.Colors.DISCORD.hex),
        CONTEST("☆", ColorUtils.Colors.FRAN_PINK.hex),
        DONATOR("❤", ColorUtils.Colors.FRAN_PINK.hex),
        TRANSLATOR("☄");

        public final Component badge;
        public final Component desc;

        Special(String unicode) {
            this(unicode, null);
        }

        Special(String unicode, Integer color) {
            this.desc = FiguraText.of("badges.special." + this.name().toLowerCase());
            Style style = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc));
            if (color != null) style = style.withColor(color);
            this.badge = Component.literal(unicode).withStyle(style);
        }
    }
}
