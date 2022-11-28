package org.moon.figura.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraResourceListener;
import org.moon.figura.utils.TextUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class Emojis {

    public static final ResourceLocation FONT = new FiguraIdentifier("emojis");

    private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(FONT);
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    private static String prefix = "\u0000";
    private static String suffix = "\u0000";

    //listener to load emojis from the resource pack
    public static final FiguraResourceListener RESOURCE_LISTENER = new FiguraResourceListener("emojis", manager -> {
        //clear old list
        EMOJI_MAP.clear();
        prefix = "\u0000";
        suffix = "\u0000";

        //get the resource
        Optional<Resource> optional = manager.getResource(new FiguraIdentifier("emojis.json"));
        if (optional.isEmpty())
            return;

        //open the resource as json
        try (InputStream stream = optional.get().open()) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

            //read the prefix and suffix
            prefix = root.get("prefix").getAsString();
            suffix = root.get("suffix").getAsString();

            //read a pair or String, List<String> from this json
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("emojis").entrySet()) {
                //the emoji is the value
                String emoji = entry.getKey();
                //and each element will be the key inside the emoji map
                for (JsonElement element : entry.getValue().getAsJsonArray())
                    EMOJI_MAP.put(element.getAsString(), emoji);
            }
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load emojis", e);
        }
    });

    public static Component applyEmojis(Component text) {
        Component newText = TextUtils.parseLegacyFormatting(text);
        MutableComponent ret = Component.empty();
        newText.visit((style, string) -> {
            ret.append(convertEmoji(string, style));
            return Optional.empty();
        }, Style.EMPTY);
        return ret;
    }

    public static Component convertEmoji(String string, Style style) {
        emoji: {
            //check first :
            String[] pre = string.split(Pattern.quote(prefix), 2);
            if (pre.length < 2)
                break emoji;

            //check if there is a second :
            if (!pre[1].contains(suffix))
                break emoji;

            //success, we can now start building our text
            MutableComponent newText = Component.literal(pre[0]).withStyle(style);

            //check second :
            String[] pos = pre[1].split(Pattern.quote(suffix), 2);
            String emoji = EMOJI_MAP.get(pos[0]);

            //success, append the emoji
            if (emoji != null) {
                newText.append(Component.literal(emoji).withStyle(STYLE));
            //fail, break if there is no remaining text to parse
            } else if (pos.length < 2) {
                break emoji;
            //otherwise append this text as is (with prefix) and re-add the suffix to the next text
            } else {
                newText.append(prefix + pos[0]);
                pos[1] = suffix + pos[1];
            }

            //parse the next text
            if (pos.length > 1)
                newText.append(convertEmoji(pos[1], style));

            //return the modified text
            return newText;
        }

        return Component.literal(string).withStyle(style);
    }
}
