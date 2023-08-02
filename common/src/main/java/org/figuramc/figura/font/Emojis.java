package org.figuramc.figura.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.*;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class Emojis {

    private static final List<EmojiContainer> EMOJIS = new ArrayList<>();
    public static final char DELIMITER = ':';
    public static final char ESCAPE = '\\';
    private static final String JSON_KEY_FRAMES = "frames";
    private static final String JSON_KEY_FRAME_TIME = "frametime";
    private static final String JSON_KEY_WIDTH = "width";
    private static final String JSON_KEY_NAMES = "names";
    private static final String JSON_KEY_SHEET_WIDTH = "width";
    private static final String JSON_KEY_SHEET_HEIGHT = "height";

    private static final String ERROR_MSG = "Invalid emoji metadata \"{}\" @ \"{}\", Reason: Field '{}' {}";

    // listener to load emojis from the resource pack
    public static final FiguraResourceListener RESOURCE_LISTENER = FiguraResourceListener.createResourceListener("emojis", manager -> {
        EMOJIS.clear();

        for (Map.Entry<ResourceLocation, Resource> emojis : manager.listResources("emojis", location -> location.getNamespace().equals(FiguraMod.MOD_ID) && location.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation location = emojis.getKey();
            String[] split = location.getPath().split("/", 2);

            if (split.length <= 1)
                continue;

            String name = split[1].substring(0, split[1].length() - 5);

            // open the resource as json
            try (InputStream stream = emojis.getValue().open()) {
                // add emoji
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                EMOJIS.add(new EmojiContainer(name, json));

                // check for duplicates
                Set<String> set = new HashSet<>();
                for (EmojiContainer emoji : EMOJIS) {
                    for (String s : emoji.unicodeLookup.keySet()) {
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
        // if the string does not contain the delimiter, then return
        if (string.indexOf(DELIMITER) == -1)
            return Component.literal(string).withStyle(style);

        // string lists, every odd index is an emoji
        List<String> strings = new ArrayList<>();

        // temp variables
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        boolean inside = false;

        // iterate over every char
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            // escape sequence
            if (c == ESCAPE) {
                escaped = !escaped;
                // append only if the next char is not the delimiter
                if (i + 1 == string.length() || string.charAt(i + 1) != DELIMITER)
                    current.append(c);
                // delimiter, only if not escaped
            } else if (c == DELIMITER && !escaped) {
                // toggle inside status
                inside = !inside;
                // and also append to the list
                strings.add(current.toString());
                current = new StringBuilder();
                // otherwise just add to the queue
            } else {
                escaped = false;
                current.append(c);

                // space character breaks current emoji parsing
                if (c == ' ' && inside) {
                    inside = false;
                    // removed last appended emoji, as were undoing the parsing of the emoji
                    String removed = strings.remove(strings.size() - 1);
                    // replace current with the removed string, adding back the delimiter and appending the current parsed text
                    current = new StringBuilder(removed).append(DELIMITER).append(current);
                }
            }
        }

        // if the queue is not flushed
        if (current.length() > 0 || inside) {
            String toAdd = current.toString();
            // if we left inside, we want to keep the text within the last index
            // also keep the delimiter since its end was not found
            if (inside) {
                int index = strings.size() - 1;
                String s = strings.get(index) + DELIMITER + toAdd;
                strings.set(index, s);
                // otherwise just add what is remaining
            } else {
                strings.add(toAdd);
            }
        }

        MutableComponent result = Component.empty().withStyle(style);

        // now we parse the list
        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            // even: append
            if (i % 2 == 0) {
                result.append(s);
                // odd: emoji
            } else {
                Component emoji = getEmoji(s);
                if (emoji != null) {
                    result.append(emoji);
                } else {
                    result.append(DELIMITER + s + DELIMITER);
                }
            }
        }

        return result;
    }

    public static Component getEmoji(String unicode) {
        for (EmojiContainer container : EMOJIS) {
            Component emoji = container.getEmoji(unicode);
            if (emoji != null) {
                return emoji;
            }
        }
        return null;
    }

    public static EmojiContainer getContainer(ResourceLocation location) {
        for (EmojiContainer container : EMOJIS) {
            if (location.equals(container.font)) {
                return container;
            }
        }
        return null;
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
            for (String s : container.unicodeLookup.keySet()) {
                if (s.startsWith(name))
                    emojis.add(DELIMITER + s + DELIMITER);
            }
        }

        return emojis;
    }

    public static void tickAnimations() {
        for (EmojiContainer container : EMOJIS) {
            container.tickAnimations();
        }
    }

    public static class EmojiContainer {
        private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE);

        private final String name;
        private final ResourceLocation font;
        private final Map<String, String> unicodeLookup = new HashMap<>(); // <EmojiName, Unicode>
        private final Map<Integer, Metadata> metadataLookup = new HashMap<>();
        private final String blacklist;

        public final int textureWidth;
        public final int textureHeight;

        public EmojiContainer(String containerName, JsonObject data) {
            this.name = containerName;
            this.font = new FiguraIdentifier("emoji_" + containerName);
            this.blacklist = data.get("blacklist").getAsString();

            // key = emoji unicode, value = array of names
            for (Map.Entry<String, JsonElement> emoji : data.get("emojis").getAsJsonObject().entrySet()) {
                String curUnicode = emoji.getKey();
                JsonElement curValue = emoji.getValue();

                JsonArray namesArray = null;
                if (curValue.isJsonArray()) {
                    namesArray = curValue.getAsJsonArray();
                } else {
                    JsonObject obj = curValue.getAsJsonObject();
                    if (JsonUtils.validate(obj, JSON_KEY_NAMES, JsonElement::isJsonArray, ERROR_MSG, curUnicode.codePointAt(0), containerName, JSON_KEY_NAMES, "must be an array") &&
                            JsonUtils.validate(obj, JSON_KEY_FRAMES, JsonElement::isJsonPrimitive, ERROR_MSG, curUnicode.codePointAt(0), containerName, JSON_KEY_FRAMES, "field must be an int") &&
                            JsonUtils.validate(obj, JSON_KEY_FRAME_TIME, JsonElement::isJsonPrimitive, ERROR_MSG, curUnicode.codePointAt(0), containerName, JSON_KEY_FRAME_TIME, "fiend must be an int")) {
                        namesArray = obj.getAsJsonArray(JSON_KEY_NAMES);
                        metadataLookup.put(curUnicode.codePointAt(0) ,new Metadata(obj));
                    }
                }

                if (namesArray != null) {
                    validateAliases(containerName, namesArray, (curAlias) -> unicodeLookup.put(curAlias, curUnicode));
                }
            }

            this.textureWidth = JsonUtils.getIntOrDefault(data, JSON_KEY_SHEET_WIDTH, 64);
            this.textureHeight = JsonUtils.getIntOrDefault(data, JSON_KEY_SHEET_HEIGHT, 64);
        }


        private static void validateAliases(String containerName, JsonArray aliasArray, Consumer<String> consumer) {
            for (JsonElement element : aliasArray) {
                String alias = element.getAsString();
                if (alias.isBlank() || alias.indexOf(' ') != -1 || alias.indexOf(DELIMITER) != -1) {
                    FiguraMod.LOGGER.warn("Invalid emoji name \"{}\" @ \"{}\"", alias, containerName);
                } else {
                    consumer.accept(alias);
                }
            }
        }

        public @Nullable Metadata getEmojiMetadata(int codepoint) {
            return metadataLookup.get(codepoint);
        }

        public void tickAnimations() {
            for (Metadata metadata : metadataLookup.values()) {
                metadata.tickAnimation();
            }
        }

        public Component getEmoji(String key) {
            String emoji = unicodeLookup.get(key);
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

        public static final class Metadata {
            public final int frames;
            public final int frameTime;
            public final int width;
            private int frameTimer;
            private int curFrame;

            public Metadata(int frames, int frameTime, int width) {
                this.frames = frames;
                this.frameTime = frameTime;
                this.width = width;
            }

            public Metadata(JsonObject entry) {
                this(entry.get(JSON_KEY_FRAMES).getAsInt(), entry.get(JSON_KEY_FRAME_TIME).getAsInt(), JsonUtils.getIntOrDefault(entry, JSON_KEY_WIDTH, 8));
            }

            public void tickAnimation() {
                frameTimer++;
                if (frameTimer >= frameTime) {
                    frameTimer -= frameTime;
                    curFrame = (curFrame + 1) % frames;
                }
            }

            public int getCurrentFrame() {
                return curFrame;
            }
        }
    }
}
