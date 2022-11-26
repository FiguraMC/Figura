package org.moon.figura.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.moon.figura.FiguraMod;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraResourceListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Emojis {

    public static final ResourceLocation FONT = new FiguraIdentifier("emojis");
    public static final String prefix = ":";
    public static final String suffix = ":";

    private static final Style STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(FONT);
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();

    //listener to load emojis from the resource pack
    public static final FiguraResourceListener RESOURCE_LISTENER = new FiguraResourceListener("emojis", manager -> {
        //clear old list
        EMOJI_MAP.clear();

        try {
            //get the resource
            Resource resource = manager.getResource(new FiguraIdentifier("emojis.json"));

            //open the resource as json
            InputStream stream = resource.getInputStream();
            //read a pair or String, List<String> from this json
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                //the emoji is the value
                String emoji = entry.getKey();
                //and each element will be the key inside the emoji map
                for (JsonElement element : entry.getValue().getAsJsonArray())
                    EMOJI_MAP.put(element.getAsString(), emoji);
            }

            stream.close();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to load emojis", e);
        }
    });

    public static Component applyEmojis(Component text) {
        MutableComponent ret = TextComponent.EMPTY.copy();
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

            MutableComponent newText = new TextComponent(pre[0]).withStyle(style).append(new TextComponent(emoji).withStyle(STYLE));
            if (pos.length > 1)
                newText.append(convertEmoji(pos[1], style));

            return newText;
        }

        return new TextComponent(string).withStyle(style);
    }
}
