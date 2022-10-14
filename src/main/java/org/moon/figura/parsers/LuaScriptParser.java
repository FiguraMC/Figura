package org.moon.figura.parsers;

import net.minecraft.nbt.ByteArrayTag;
import org.moon.figura.FiguraMod;
import org.moon.figura.config.Config;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaScriptParser {

    // regex minify constants

    private static final Pattern string = Pattern.compile("\"(\\\\|\\\"|[^\"\n\r])*?\"|'(\\\\|\\'|[^'\n\r])*?'".stripIndent(), Pattern.MULTILINE);
    private static final Pattern multilineString = Pattern.compile("\\[(?<s>=*)\\[.*?](\\k<s>)]", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern comments = Pattern.compile("--[^\n]*$", Pattern.MULTILINE);
    private static final Pattern multilineComment = Pattern.compile("--\\[(?<s>=*)\\[.*?](\\k<s>)]", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern newlines = Pattern.compile("^[\t ]*((\n|\n\r|\r\n)[\t ]*)?");
    private static final Pattern words = Pattern.compile("[a-zA-Z_]\\w*");
    private static final Pattern trailingNewlines = Pattern.compile("\n*$");
    private static final Pattern sheBangs = Pattern.compile("^#![^\n]*");

    // aggressive minify constants

    private static final Pattern allStrings = Pattern.compile(string.pattern() + "|" + multilineString.pattern());
    private static final Pattern whitespacePlus = Pattern.compile("[ \n]+");
    private static final Pattern nameOops = Pattern.compile("\\w{2}");

    //parsing data
    private static boolean error;

    public static ByteArrayTag parseScript(String name, String script) {
        error = true;
        ByteArrayTag out = new ByteArrayTag((switch (Config.FORMAT_SCRIPT.asInt()) {
            case 0 -> noMinifier(script);
            case 1 -> regexMinify(name, script);
            case 2 -> aggressiveMinify(name, script);
            default -> throw new IllegalStateException("Format_SCRIPT should not be %d, expecting 0 to %d".formatted(Config.FORMAT_SCRIPT.asInt(), Config.FORMAT_SCRIPT.enumList.size() - 1));
        }).getBytes(StandardCharsets.UTF_8));

        if (error) FiguraMod.LOGGER.error("Failed to minify the script, likely to be syntax error");
        return out;
    }

    private static String noMinifier(String script) {
        error = false;
        return script;
    }

    private static String regexMinify(String name, String script) {
        StringBuilder builder = new StringBuilder(script);
        for (int i = 0; i < builder.length(); i++) {
            switch (builder.charAt(i)) {
                case '#' -> {
                    if (i > 0)
                        continue;

                    Matcher matcher = sheBangs.matcher(builder);
                    if (matcher.find())
                        builder.delete(0, matcher.end());
                }
                case '\'', '"' -> {
                    Matcher matcher = string.matcher(builder);
                    if (!matcher.find(i) || !(matcher.start() == i))
                        return script;

                    i = matcher.end() - 1;
                }
                case '[' -> {
                    Matcher matcher = multilineString.matcher(builder);
                    if (matcher.find(i) && matcher.start() == i)
                        i = matcher.end() - 1;
                }
                case '-' -> {
                    if (i == builder.length() - 1)
                        return script;

                    Matcher multiline = multilineComment.matcher(builder);
                    if (multiline.find(i) && multiline.start() == i) {
                        int breaks = builder.substring(i, multiline.end()).split("\n").length - 1;
                        builder.delete(i, multiline.end()).insert(i, "\n".repeat(breaks));
                        i--;
                        continue;
                    }

                    Matcher comment = comments.matcher(builder);
                    if (comment.find(i) && comment.start() == i) {
                        builder.delete(comment.start(), comment.end());
                        i--;
                    }
                }
                case ' ', '\t', '\n', '\r' -> {
                    Matcher newline = newlines.matcher(builder.substring(i));
                    if (newline.find())
                        if (newline.start() == 0)
                            builder.delete(i, i + newline.end()).insert(i, Optional.ofNullable(newline.group(1)).map(t -> "\n").orElse(" "));
                        else
                            FiguraMod.LOGGER.warn("script TODO appears to have invalid new line configuration");
                }
                default -> {
                    Matcher word = words.matcher(builder);
                    if (word.find(i) && word.start() == i)
                        i = word.end() - 1;
                }
            }
        }

        Matcher trailingNewline = trailingNewlines.matcher(builder);
        if (trailingNewline.find())
            builder.replace(trailingNewline.start(), trailingNewline.end(), "\n");

        FiguraMod.debug("Script \"{}\" minified from {} to {} using LIGHT mode", name, script.length(), builder.length());

        error = false;
        return builder.toString();
    }

    private static String aggressiveMinify(String name, String script) {
        String start = regexMinify(name, script);
        StringBuilder builder = new StringBuilder(start);

        for (int i = 0; i < builder.length(); i++) {
            switch (builder.charAt(i)) {
                case '\'', '"', '[' -> {
                    Matcher matcher = allStrings.matcher(builder);
                    if (matcher.find(i) && matcher.start() == i)
                        i = matcher.end() - 1;
                }
                case ' ', '\n' -> {
                    Matcher matcher = whitespacePlus.matcher(builder);
                    if (matcher.find(i) && matcher.start() == i)
                        builder.delete(i, matcher.end()).insert(i, matcher.start() > 0 && matcher.start() + 1 < builder.length() && nameOops.matcher(builder.substring(matcher.start() - 1, matcher.start() + 1)).matches() ? " " : "");
                }
                default -> {
                    Matcher word = words.matcher(builder);
                    if (word.find(i) && word.start() == i)
                        i = word.end() - 1;
                }
            }
        }

        FiguraMod.debug("Script \"{}\" minified from {} to {} using HEAVY mode", name, script.length(), builder.length());
        return builder.toString();
    }
}
