package org.moon.figura.avatar;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.trust.Trust;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

import java.util.BitSet;
import java.util.Optional;
import java.util.UUID;

public class Badges {

    private static final String BADGES_REGEX = ".*(\\$\\{badges}|\\$\\{segdab}).*";

    public static final ResourceLocation FONT = new FiguraIdentifier("badges");

    public static Component fetchBadges(UUID id) {
        MutableComponent badges = Component.empty().withStyle(Style.EMPTY.withFont(FONT).withColor(ChatFormatting.WHITE));

        if (TrustManager.get(id).getGroup() == Trust.Group.BLOCKED)
            return badges;

        //get user data
        Pair<BitSet, BitSet> pair = AvatarManager.getBadges(id);
        if (pair == null)
            return badges;

        //avatar badges
        Avatar avatar = AvatarManager.getAvatarForPlayer(id);
        if (avatar != null) {

            // -- loading -- //

            if (!avatar.loaded)
                badges.append(Component.literal(Integer.toHexString(Math.abs(FiguraMod.ticks) % 16)));

            // -- mark -- //

            else if (avatar.nbt != null) {
                Pride[] pride = Pride.values();

                //trust
                if (avatar.trustIssues)
                    badges.append(System.TRUST.badge);

                //error
                else if (avatar.scriptError)
                    badges.append(System.ERROR.badge);

                //version
                else if (avatar.versionStatus > 0)
                    badges.append(System.WARNING.badge);

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
        return TextUtils.replaceInText(text, "[❗❌\uD83D\uDEE1☄❤☆★0-9a-f]", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(FONT));
    }

    public static Pair<BitSet, BitSet> emptyBadges() {
        return Pair.of(new BitSet(Pride.values().length), new BitSet(Special.values().length));
    }

    public static boolean hasCustomBadges(Component text) {
        return text.visit((style, string) -> string.matches(BADGES_REGEX) ? FormattedText.STOP_ITERATION : Optional.empty(), Style.EMPTY).isPresent();
    }

    public static Component appendBadges(Component text, UUID id, boolean allow) {
        Component badges = allow ? fetchBadges(id) : Component.empty();
        boolean custom = hasCustomBadges(text);

        //no custom badges text
        if (!custom)
            return badges.getString().isBlank() ? text : text.copy().append(" ").append(badges);

        text = TextUtils.replaceInText(text, "\\$\\{badges\\}", badges);
        text = TextUtils.replaceInText(text, "\\$\\{segdab\\}", TextUtils.reverse(badges));

        return text;
    }

    public enum System {
        DEFAULT("△"),
        TRUST("\uD83D\uDEE1"),
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
