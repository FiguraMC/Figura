package org.moon.figura.utils;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {

    public static final ResourceLocation FIGURA_FONT = new FiguraIdentifier("default");
    public static final int TAB_SPACING = 2;

    public static String noBadges4U(String string) {
        return string.replaceAll("([▲!❤☆✯★]|\\\\u(?i)(25B2|0021|2764|2606|272F|2605))", "\uFFFD");
    }

    public static List<Component> splitText(Component text, String regex) {
        //list to return
        ArrayList<Component> textList = new ArrayList<>();

        //current line variable
        MutableComponent currentText = TextComponent.EMPTY.copy();

        //iterate over the text
        for (Component entry : text.toFlatList(text.getStyle())) {
            //split text based on regex
            String entryString = entry.getString();
            String[] lines = entryString.split("((?<=" + regex + ")|(?=" + regex + "))");

            //iterate over the split text
            for (int i = 0; i < lines.length; i++) {
                //if it is not the first iteration, add to return list and reset the line variable
                if (i != 0) {
                    textList.add(currentText.copy());
                    currentText = TextComponent.EMPTY.copy();
                }

                //append text with the line text
                if (!lines[i].matches(regex))
                    currentText.append(new TextComponent(lines[i]).setStyle(entry.getStyle()));
            }

            //if the text ends with the split pattern, add to return list and reset the line variable
            if (entryString.matches(".*" + regex + "$")) {
                textList.add(currentText.copy());
                currentText = TextComponent.EMPTY.copy();
            }
        }
        //add the last text iteration then return
        textList.add(currentText);
        return textList;
    }

    public static Component removeClickableObjects(Component text) {
        //text to return
        MutableComponent finalText = TextComponent.EMPTY.copy();

        //iterate over the text
        for (Component entry : text.toFlatList(text.getStyle())) {
            //remove click events
            Component removed = new TextComponent(entry.getString()).setStyle(entry.getStyle().withClickEvent(null));

            //append text to return
            finalText.append(removed);
        }

        //return text
        return finalText;
    }

    public static Component tryParseJson(String text) {
        //text to return
        Component finalText;

        try {
            //attempt to parse json
            finalText = Component.Serializer.fromJson(new StringReader(text));

            //if failed, throw a dummy exception
            if (finalText == null)
                throw new Exception("Error parsing JSON string");
        } catch (Exception ignored) {
            //on any exception, make the text as-is
            finalText = new TextComponent(text);
        }

        //return text
        return finalText;
    }

    public static Component replaceInText(Component text, String regex, Object replacement) {
        Component replace = replacement instanceof Component c ? c : new TextComponent(String.valueOf(replacement));

        //split the text based on the regex pattern
        List<Component> list = splitText(text, regex);

        //text to return
        MutableComponent finalText = TextComponent.EMPTY.copy();

        //iterate over the split text
        for (int i = 0; i < list.size(); i++) {
            //append the split text on the return text
            finalText.append(list.get(i));

            //if it is not the last iteration, append the replacement text
            if (i < list.size() - 1)
                finalText.append(replace);
        }

        //return the text
        return finalText;
    }

    public static Component trimToWidthEllipsis(Font font, Component text, int width) {
        //return text without changes if it is not larger than width
        if (font.width(text.getVisualOrderText()) <= width)
            return text;

        //get ellipsis size
        Component dots = Component.nullToEmpty("...");
        int size = font.width(dots.getVisualOrderText());

        //trim and return modified text
        String trimmed = font.substrByWidth(text, width - size).getString();
        return new TextComponent(trimmed).setStyle(text.getStyle()).append(dots);
    }

    public static Component replaceTabs(Component text) {
        return TextUtils.replaceInText(text, "\\t", " ".repeat(TAB_SPACING));
    }
}
