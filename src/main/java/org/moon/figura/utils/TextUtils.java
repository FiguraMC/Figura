package org.moon.figura.utils;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.moon.figura.FiguraMod;

import java.util.ArrayList;
import java.util.List;

public class TextUtils {

    public static final Identifier FIGURA_FONT = new Identifier(FiguraMod.MOD_ID, "default");

    public static String noBadges4U(String string) {
        return string.replaceAll("([▲!❤☆✯★]|\\\\u(?i)(25B2|0021|2764|2606|272F|2605))", "\uFFFD");
    }

    public static List<Text> splitText(Text text, String regex) {
        //list to return
        ArrayList<Text> textList = new ArrayList<>();

        //current line variable
        MutableText currentText = LiteralText.EMPTY.copy();

        //iterate over the text
        for (Text entry : text.getWithStyle(text.getStyle())) {
            //split text based on regex
            String entryString = entry.getString();
            String[] lines = entryString.split(regex);

            //iterate over the split text
            for (int i = 0; i < lines.length; i++) {
                //if it is not the first iteration, add to return list and reset the line variable
                if (i != 0) {
                    textList.add(currentText.shallowCopy());
                    currentText = LiteralText.EMPTY.copy();
                }

                //append text with the line text
                currentText.append(new LiteralText(lines[i]).setStyle(entry.getStyle()));
            }

            //if the text ends with the split pattern, add to return list and reset the line variable
            if (entryString.matches(".*" + regex + "$")) {
                textList.add(currentText.shallowCopy());
                currentText = LiteralText.EMPTY.copy();
            }
        }
        //add the last text iteration then return
        textList.add(currentText);
        return textList;
    }

    public static Text removeClickableObjects(Text text) {
        //text to return
        MutableText finalText = LiteralText.EMPTY.copy();

        //iterate over the text
        for (Text entry : text.getWithStyle(text.getStyle())) {
            //remove click events
            Text removed = new LiteralText(entry.getString()).setStyle(entry.getStyle().withClickEvent(null));

            //append text to return
            finalText.append(removed);
        }

        //return text
        return finalText;
    }

    public static Text tryParseJson(String text) {
        //text to return
        Text finalText;

        try {
            //attempt to parse json
            finalText = Text.Serializer.fromJson(new StringReader(text));

            //if failed, throw a dummy exception
            if (finalText == null)
                throw new Exception("Error parsing JSON string");
        } catch (Exception ignored) {
            //on any exception, make the text as-is
            finalText = new LiteralText(text);
        }

        //return text
        return finalText;
    }

    public static Text replaceInText(Text text, String regex, Text replacement) {
        //split the text based on the regex pattern
        List<Text> list = splitText(text, regex);

        //text to return
        MutableText finalText = LiteralText.EMPTY.copy();

        //iterate over the split text
        for (int i = 0; i < list.size(); i++) {
            //append the split text on the return text
            finalText.append(list.get(i));

            //if it is not the last iteration, append the replacement text
            if (i < list.size() - 1)
                finalText.append(replacement);
        }

        //return the text
        return finalText;
    }

    public static Text trimToWidthEllipsis(TextRenderer textRenderer, Text text, int width) {
        //return text without changes if it is not larger than width
        if (textRenderer.getWidth(text.asOrderedText()) <= width)
            return text;

        //get ellipsis size
        Text dots = Text.of("...");
        int size = textRenderer.getWidth(dots.asOrderedText());

        //trim and return modified text
        String trimmed = textRenderer.trimToWidth(text, width - size).getString();
        return new LiteralText(trimmed).setStyle(text.getStyle()).append(dots);
    }
}
