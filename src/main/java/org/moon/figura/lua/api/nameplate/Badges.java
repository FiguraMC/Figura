package org.moon.figura.lua.api.nameplate;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.TextUtils;

import java.util.BitSet;
import java.util.HashMap;
import java.util.UUID;

public class Badges {

    private static final HashMap<UUID, BitSet> badgesMap = new HashMap<>();

    public static Component fetchBadges(Avatar avatar) {
        if (avatar == null)
            return Component.empty();

        MutableComponent badges = Component.literal(" ").withStyle(Style.EMPTY.withFont(TextUtils.FIGURA_FONT).withColor(ChatFormatting.WHITE));

        UUID id = avatar.owner;
        BitSet badgesSet = badgesMap.get(id);
        if (badgesSet == null) {
            badgesSet = new BitSet(count());
            badgesMap.put(id, badgesSet);
        }

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
                    if (badgesSet.get(i)) {
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
        for (int i = 0; i < special.length; i++) {
            if (badgesSet.get(i + pride.length))
                badges.append(Component.literal(special[i].badge).withStyle(Style.EMPTY.withColor(special[i].color())));
        }

        return badges.getString().isBlank() ? Component.empty() : badges;
    }

    public static void load(UUID id, BitSet bitSet) {
        BitSet set = badgesMap.getOrDefault(id, new BitSet(count()));
        set.or(bitSet);
        badgesMap.put(id, set);
    }

    public static int count() {
        return Pride.values().length + Special.values().length;
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
}
