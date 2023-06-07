package org.moon.figura.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.TextUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Emojis {

    private static final List<EmojiContainer> EMOJIS = new ArrayList<>();
    public static final char DELIMITER = ':';
    public static final char ESCAPE = '\\';

    //listener to load emojis from the resource pack
    public static final FiguraResourceListener RESOURCE_LISTENER = new FiguraResourceListener("emojis", manager -> {
        EMOJIS.clear();

        for (Map.Entry<ResourceLocation, Resource> emojis : manager.listResources("emojis", location -> location.getNamespace().equals(FiguraMod.MOD_ID) && location.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation location = emojis.getKey();
            String[] split = location.getPath().split("/", 2);

            if (split.length <= 1)
                continue;

            String name = split[1].substring(0, split[1].length() - 5);

            //open the resource as json
            try (InputStream stream = emojis.getValue().open()) {
                //add emoji
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                EMOJIS.add(new EmojiContainer(name, json));

                //check for duplicates
                Set<String> set = new HashSet<>();
                for (EmojiContainer emoji : EMOJIS) {
                    for (String s : emoji.map.keySet()) {
                        if (!set.add(s)) {
                            FiguraMod.LOGGER.warn("Duplicate emoji id registered {}", s);
                        }
                    }
                }
            } catch (Exception e) {
                FiguraMod.LOGGER.error("Failed to load {} emojis", name, e);
            }
        }
    });

    public static MutableComponent applyEmojis(Component text) {
        Component newText = TextUtils.parseLegacyFormatting(text);
        MutableComponent ret = Component.empty();
        newText.visit((style, string) -> {
            ret.append(convertEmoji(string, style));
            return Optional.empty();
        }, Style.EMPTY);
        return ret;
    }

    private static MutableComponent convertEmoji(String string, Style style) {
        //if the string does not contain the delimiter, then return
        if (string.indexOf(DELIMITER) == -1)
            return Component.literal(string).withStyle(style);

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

                //space character breaks current emoji parsing
                if (c == ' ' && inside) {
                    inside = false;
                    //removed last appended emoji, as were undoing the parsing of the emoji
                    String removed = strings.remove(strings.size() - 1);
                    //replace current with the removed string, adding back the delimiter and appending the current parsed text
                    current = new StringBuilder(removed).append(DELIMITER).append(current);
                }
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

        MutableComponent result = Component.empty().withStyle(style);

        //now we parse the list
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            //even: append
            if (i % 2 == 0) {
                result.append(s);
            //odd: emoji
            } else {
                apply: {
                    for (EmojiContainer container : EMOJIS) {
                        Component emoji = container.getEmoji(s);
                        if (emoji != null) {
                            //emoji found, add it and break
                            result.append(emoji);
                            break apply;
                        }
                    }
                    //emoji not found, so we add the unformatted text
                    result.append(DELIMITER + s + DELIMITER);
                }
            }
        }

        return result;
    }

    public static Component removeBlacklistedEmojis(Component text) {
        for (EmojiContainer container : EMOJIS)
            text = container.blacklist(text);
        return text;
    }

    public static List<String> getMatchingEmojis(String query) {
        if (query.length() == 0 || query.charAt(0) != DELIMITER)
            return List.of();

        String name = query.substring(1);
        List<String> emojis = new ArrayList<>();

        for (EmojiContainer container : EMOJIS) {
            for (String s : container.map.keySet()) {
                if (s.startsWith(name))
                    emojis.add(DELIMITER + s + DELIMITER);
            }
        }

        return emojis;
    }

    private static class EmojiContainer {
        private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE);

        private final String name;
        private final ResourceLocation font;
        private final Map<String, String> map = new HashMap<>(); //<EmojiName, Unicode>
        private final String blacklist;

        public EmojiContainer(String name, JsonObject data) {
            this.name = name;
            this.font = new FiguraIdentifier("emoji_" + name);
            this.blacklist = data.get("blacklist").getAsString();

            //key = emoji unicode, value = array of names
            for (Map.Entry<String, JsonElement> emoji : data.get("emojis").getAsJsonObject().entrySet()) {
                String unicode = emoji.getKey();
                for (JsonElement element : emoji.getValue().getAsJsonArray()) {
                    String key = element.getAsString();
                    if (key.isBlank() || key.indexOf(' ') != -1 || key.indexOf(DELIMITER) != -1) {
                        FiguraMod.LOGGER.warn("Invalid emoji name \"{}\" @ \"{}\"", key, name);
                    } else {
                        map.put(key, unicode);
                    }
                }
            }
        }

        public Component getEmoji(String key) {
            String emoji = map.get(key.toLowerCase());
            if (emoji == null)
                return null;
            return Component.literal(emoji).withStyle(STYLE.withFont(font).withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(DELIMITER + key + DELIMITER)
                            .append("\n")
                            .append(FiguraText.of("emoji." + name).withStyle(ChatFormatting.DARK_GRAY)))
            ));
        }

        public Component blacklist(Component text) {
            if (blacklist.isBlank())
                return text;
            return TextUtils.replaceInText(text, "[" + blacklist + "]", TextUtils.UNKNOWN, (s, style) -> style.getFont().equals(font), Integer.MAX_VALUE);
        }
    }
}
