package org.moon.figura.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Emojis {

    public static final ResourceLocation FONT = new FiguraIdentifier("emojis");
    public static final String prefix = ":";
    public static final String suffix = ":";

    private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(FONT);
    private static final Map<String, String> EMOJI_MAP = new HashMap<>() {{
        put("amongus", "ඞ");
        put("burned", "\uD83D\uDD25");
        put("plant", "\uD83C\uDF31");
        put("hat", "\uD83C\uDFA9"); put("gn", "\uD83C\uDFA9");
        put("ranch", "\uD83C\uDF7C");
        put("toast", "\uD83C\uDF5E");
        put("egg", "\uD83E\uDD5A"); put("ovo", "\uD83E\uDD5A");
        put("cake", "\uD83C\uDF70");
        put("burger", "\uD83C\uDF54"); put("hamburger", "\uD83C\uDF54");
        put("shrimp", "\uD83E\uDD90");
        put("moon", "\uD83C\uDF19"); put("kotm", "\uD83C\uDF19"); put("lua", "\uD83C\uDF19");
        put("darkness", "\uD83C\uDF00"); put("shadow", "\uD83C\uDF00");
        put("money", "\uD83D\uDCB5"); put("dollar", "\uD83D\uDCB5"); put("cash", "\uD83D\uDCB5");
        put("coffee", "☕"); put("java", "☕");
        put("lobster", "\uD83E\uDD9E");
        put("troll", "\uD83D\uDC7A"); put("trol", "\uD83D\uDC7A");
        put("nini", "\uD83D\uDCA4"); put("sleep", "\uD83D\uDCA4");
        put("skull", "\uD83D\uDC80"); put("forgor", "\uD83D\uDC80");
        put("cookie", "\uD83C\uDF6A");
        put("this", "\uD83D\uDD3A");
    }};

    public static Component applyEmojis(Component text) {
        MutableComponent ret = Component.empty();
        text.visit((style, string) -> {
            ret.append(convertEmoji(string, style));
            return Optional.empty();
        }, Style.EMPTY);
        return ret;
    }

    public static Component convertEmoji(String string, Style style) {
        emoji: {
            String[] pre = string.split(prefix, 2);
            if (pre.length < 2)
                break emoji;

            String[] pos = pre[1].split(suffix, 2);
            String emoji = EMOJI_MAP.get(pos[0]);
            if (emoji == null)
                break emoji;

            MutableComponent newText = Component.literal(pre[0]).withStyle(style).append(Component.literal(emoji).withStyle(STYLE));
            if (pos.length > 1)
                newText.append(convertEmoji(pos[1], style));

            return newText;
        }

        return Component.literal(string).withStyle(style);
    }
}
