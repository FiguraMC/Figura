package org.moon.figura.utils;

import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public class TextUtils {

    public static final Component TAB = new FiguraText("tab");
    public static final Component ELLIPSIS = new FiguraText("ellipsis");
    public static final Component UNKNOWN = new TextComponent("�").withStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT));

    public static List<Component> splitText(Component text, String regex) {
        //list to return
        ArrayList<Component> textList = new ArrayList<>();

        //current line variable
        MutableComponent currentText = TextComponent.EMPTY.copy();

        //iterate over the text
        for (Component entry : text.toFlatList(text.getStyle())) {
            //split text based on regex
            String entryString = entry.getString();
            String[] lines = entryString.split(regex, -1);

            //iterate over the split text
            for (int i = 0; i < lines.length; i++) {
                //if it is not the first iteration, add to return list and reset the line variable
                if (i != 0) {
                    textList.add(currentText.copy());
                    currentText = TextComponent.EMPTY.copy();
                }

                //append text with the line text
                currentText.append(new TextComponent(lines[i]).setStyle(entry.getStyle()));
            }
        }
        //add the last text iteration then return
        textList.add(currentText);
        return textList;
    }

    public static Component removeClickableObjects(Component text) {
        MutableComponent ret = TextComponent.EMPTY.copy();
        text.visit((style, string) -> {
            ret.append(new TextComponent(string).withStyle(style.withClickEvent(null)));
            return Optional.empty();
        }, Style.EMPTY);
        return ret;
    }

    public static Component tryParseJson(String text) {
        if (text == null)
            return TextComponent.EMPTY.copy();

        //text to return
        Component finalText;

        try {
            //check if its valid json text
            JsonParser.parseString(text);

            //attempt to parse json
            finalText = Component.Serializer.fromJsonLenient(text);

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
        return replaceInText(text, regex, replacement, (s, style) -> true);
    }

    public static Component replaceInText(Component text, String regex, Object replacement, BiPredicate<String, Style> predicate) {
        //fix replacement object
        Component replace = replacement instanceof Component c ? c : new TextComponent(replacement.toString());
        MutableComponent ret = TextComponent.EMPTY.copy();

        text.visit((style, string) -> {
            //test predicate
            if (!predicate.test(string, style)) {
                ret.append(new TextComponent(string).withStyle(style));
                return Optional.empty();
            }

            //split
            String[] split = string.split("((?<=" + regex + ")|(?=" + regex + "))");
            for (String s : split) {
                //append the text if it does not match the split, otherwise append the replacement instead
                if (!s.matches(regex))
                    ret.append(new TextComponent(s).withStyle(style));
                else
                    ret.append(TextComponent.EMPTY.copy().withStyle(style).append(replace));
            }

            return Optional.empty();
        }, Style.EMPTY);

        return ret;
    }

    public static Component trimToWidthEllipsis(Font font, Component text, int width, Component ellipsis) {
        //return text without changes if it is not larger than width
        if (font.width(text.getVisualOrderText()) <= width)
            return text;

        //add ellipsis
        return addEllipsis(font, text, width, ellipsis);
    }

    public static Component addEllipsis(Font font, Component text, int width, Component ellipsis) {
        //trim with the ellipsis size and return the modified text
        FormattedText trimmed = font.substrByWidth(text, width - font.width(ellipsis));
        return formattedTextToText(trimmed).copy().append(ellipsis);
    }

    public static Component replaceTabs(Component text) {
        return TextUtils.replaceInText(text, "\\t", TAB);
    }

    public static List<FormattedCharSequence> warpTooltip(Component text, Font font, int mousePos, int screenWidth) {
        //first split the new line text
        List<Component> splitText = TextUtils.splitText(text, "\n");

        //get the possible tooltip width
        int left = mousePos - 16;
        int right = screenWidth - mousePos - 12;

        //get largest text size
        int largest = getWidth(splitText, font);

        //get the optimal side for warping
        int side = largest <= right ? right : largest <= left ? left : Math.max(left, right);

        //warp the unmodified text
        return warpText(text, side, font);
    }

    //get the largest text width from a list
    public static int getWidth(List<?> text, Font font) {
        int width = 0;

        for (Object object : text) {
            int w;
            if (object instanceof Component component) //instanceof switch case only for java 17 experimental ;-;
                w = font.width(component);
            else if (object instanceof FormattedCharSequence charSequence)
                w = font.width(charSequence);
            else if (object instanceof String s)
                w = font.width(s);
            else
                w = 0;

            width = Math.max(width, w);
        }
        return width;
    }

    public static Component replaceStyle(Component text, Style newStyle) {
        MutableComponent ret = TextComponent.EMPTY.copy();

        List<Component> list = text.toFlatList(text.getStyle());
        for (Component component : list)
            ret.append(component.copy().withStyle(newStyle));

        return ret;
    }

    public static List<FormattedCharSequence> warpText(Component text, int width, Font font) {
        List<FormattedCharSequence> warp = new ArrayList<>();
        font.getSplitter().splitLines(text, width, Style.EMPTY, (formattedText, aBoolean) -> warp.add(Language.getInstance().getVisualOrder(formattedText)));
        return warp;
    }

    public static Component charSequenceToText(FormattedCharSequence charSequence) {
        MutableComponent builder = TextComponent.EMPTY.copy();
        charSequence.accept((index, style, codePoint) -> {
            builder.append(new TextComponent(String.valueOf(Character.toChars(codePoint))).withStyle(style));
            return true;
        });
        return builder;
    }

    public static Component formattedTextToText(FormattedText formattedText) {
        MutableComponent builder = TextComponent.EMPTY.copy();
        formattedText.visit((style, string) -> {
            builder.append(new TextComponent(string).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return builder;
    }

    public static Component substring(Component text, int beginIndex, int endIndex) {
        StringBuilder counter = new StringBuilder();
        MutableComponent builder = TextComponent.EMPTY.copy();
        text.visit((style, string) -> {
            int index = counter.length();
            int len = string.length();

            if (index <= endIndex && index + len >= beginIndex) {
                int sub = Math.max(beginIndex - index, 0);
                int top = Math.min(endIndex - index, len);
                builder.append(new TextComponent(string.substring(sub, top)).withStyle(style));
            }

            counter.append(string);
            return counter.length() > endIndex ? FormattedText.STOP_ITERATION : Optional.empty();
        }, Style.EMPTY);
        return builder;
    }

    public static Component parseLegacyFormatting(Component text) {
        MutableComponent builder = TextComponent.EMPTY.copy();
        text.visit((style, string) -> {
            formatting: {
                //check for the string have the formatting char
                if (!string.contains("§"))
                    break formatting;

                //split the string at the special char
                String[] split = string.split("§");
                if (split.length < 2)
                    break formatting;

                //creates a new text with the left part of the string
                MutableComponent newText = new TextComponent(split[0]).withStyle(style);

                //if right part has text
                for (int i = 1; i < split.length; i++) {
                    String s = split[i];

                    if (s.length() == 0)
                        continue;

                    //get the formatting code and apply to the style
                    ChatFormatting formatting = ChatFormatting.getByCode(s.charAt(0));
                    if (formatting != null)
                        style = style.applyLegacyFormat(formatting);

                    //create right text, and yeet the formatting code
                    newText.append(new TextComponent(s.substring(1)).withStyle(style));
                }

                builder.append(newText);
                return Optional.empty();
            }

            builder.append(new TextComponent(string).withStyle(style));
            return Optional.empty();
        }, Style.EMPTY);
        return builder;
    }
}
