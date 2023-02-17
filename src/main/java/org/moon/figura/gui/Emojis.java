package org.moon.figura.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.TextUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Emojis {

    public static final ResourceLocation FONT = new FiguraIdentifier("emoji");

    private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(FONT);
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    private static final char DELIMITER = ':';
    private static final char ESCAPE = '\\';

    //listener to load emojis from the resource pack
    public static final FiguraResourceListener RESOURCE_LISTENER = new FiguraResourceListener("emojis", manager -> {
        //open the resource as json
        try (InputStream stream = manager.getResource(new FiguraIdentifier("emojis.json")).getInputStream()) {
            JsonObject emojis = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

            //read a pair or String, List<String> from this json
            for (Map.Entry<String, JsonElement> entry : emojis.entrySet()) {
                //the emoji is the value
                String emoji = entry.getKey();
                //and each element will be the key inside the emoji map
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    String key = element.getAsString();
                    if (EMOJI_MAP.containsKey(key))
                        FiguraMod.LOGGER.warn("Duplicate emoji id \"" + key + "\" for emoji \"" + emoji + "\"");
                    EMOJI_MAP.put(key, emoji);
                }
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load emojis", e);
        }
    });

    public static Component applyEmojis(Component text) {
        Component newText = TextUtils.parseLegacyFormatting(text);
        MutableComponent ret = TextComponent.EMPTY.copy();
        newText.visit((style, string) -> {
            ret.append(convertEmoji(string, style));
            return Optional.empty();
        }, Style.EMPTY);
        return ret;
    }

    private static Component convertEmoji(String string, Style style) {
        //if the string does not contain the delimiter, then return
        if (!string.contains(":"))
            return new TextComponent(string).withStyle(style);

        //string lists, every odd index is an emoji
        List<String> strings = new ArrayList<>();

        //temp variables
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        boolean inside = false;

        //iterate over every char
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            //escape sequence
            if (c == ESCAPE) {
                escaped = !escaped;
                //append only if the next char is not the delimiter
                if (i + 1 == string.length() || string.charAt(i + 1) != DELIMITER)
                    current.append(c);
            //delimiter, only if not escaped
            } else if (c == DELIMITER && !escaped) {
                //toggle inside status
                inside = !inside;
                //and also append to the list
                strings.add(current.toString());
                current = new StringBuilder();
            //otherwise just add to the queue
            } else {
                escaped = false;
                current.append(c);
            }
        }

        //if the queue is not flushed
        if (current.length() > 0 || inside) {
            String toAdd = current.toString();
            //if we left inside, we want to keep the text within the last index
            //also keep the delimiter since its end was not found
            if (inside) {
                int index = strings.size() - 1;
                String s = strings.get(index) + DELIMITER + toAdd;
                strings.set(index, s);
            //otherwise just add what is remaining
            } else {
                strings.add(toAdd);
            }
        }

        MutableComponent result = TextComponent.EMPTY.copy().withStyle(style);

        //now we parse the list
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            //even: append
            if (i % 2 == 0) {
                result.append(s);
            //odd: emoji
            } else {
                String emoji = EMOJI_MAP.get(s.toLowerCase());
                String quoted = DELIMITER + s + DELIMITER;
                //emoji not found, so we add the unformatted text
                if (emoji == null) {
                    result.append(quoted);
                //add the emoji
                } else {
                    result.append(new TextComponent(emoji).withStyle(STYLE.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(quoted)))));
                }
            }
        }

        return result;
    }

    public static Component removeBlacklistedEmojis(Component text) {
        return text; // TextUtils.replaceInText(text, "[Δ]", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(FONT), Integer.MAX_VALUE);
    }
}
