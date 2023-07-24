package org.figuramc.figura.parsers;

import net.minecraft.nbt.ByteArrayTag;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.luaj.vm2.ast.Chunk;
import org.luaj.vm2.ast.NameResolver;
import org.luaj.vm2.parser.LuaParser;
import org.luaj.vm2.parser.ParseException;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaScriptParser {

    // regex minify constants

    private static final Pattern string = Pattern.compile("([\"'])(?:\\\\(?:z\\s*|\\d{1,3}|x[a-fA-F\\d]{2}|[\\\\\"'\\n\\rabfnrtv])|(?:(?!\\1|[\\\\\\n\\r]).)*+)*?\\1", Pattern.MULTILINE);
    private static final Pattern multilineString = Pattern.compile("(?s:\\[(=*)\\[.*?]\\1])", Pattern.MULTILINE);
    private static final Pattern comments = Pattern.compile("--[^\n]*$", Pattern.MULTILINE);
    private static final Pattern multilineComment = Pattern.compile("--(?s:\\[(?<s>=*)\\[.*?](\\k<s>)])", Pattern.MULTILINE);
    private static final Pattern newlines = Pattern.compile("^[\t ]*((\n|\n\r|\r\n|\r)[\t ]*)?");
    private static final Pattern words = Pattern.compile("[a-zA-Z_]\\w*");
    private static final Pattern trailingNewlines = Pattern.compile("\n*$");
    private static final Pattern sheBangs = Pattern.compile("^#![^\n]*");

    // aggressive minify constants

    private static final Pattern allStrings = Pattern.compile(string.pattern() + "|" + multilineString.pattern().replace('1', '2'), Pattern.MULTILINE);
    private static final Pattern whitespacePlus = Pattern.compile("[ \n]+");
    private static final Pattern nameOops = Pattern.compile("\\w{2}");

    // parsing data
    private static boolean error;

    public static ByteArrayTag parseScript(String name, String script) {
        error = true;
        String minified = switch (Configs.FORMAT_SCRIPT.value) {
            case 0 -> noMinifier(script);
            case 1 -> regexMinify(name, script);
            case 2 -> aggressiveMinify(name, script);
            case 3 -> ASTMinify(name, script);
            default -> throw new IllegalStateException("Format_SCRIPT should not be %d, expecting 0 to %d".formatted(Configs.FORMAT_SCRIPT.value, Configs.FORMAT_SCRIPT.enumList.size() - 1));
        };
        ByteArrayTag out;
        if (error) {
            FiguraMod.LOGGER.warn("Failed to minify the script, likely to be syntax error");
            out = new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
        } else {
            out = new ByteArrayTag(minified.getBytes(StandardCharsets.UTF_8));
        }
        return out;
    }

    private static String noMinifier(String script) {
        error = false;
        return script;
    }

    private static String regexMinify(String name, String script) {
        StringBuilder builder = new StringBuilder(script);
        int ogLen = script.length();

        for (int i = 0; i < builder.length(); i++) {
            if (builder.length() > ogLen)
                throw new RuntimeException("Script minifier stopped due to a possible infinite loop when parsing \"" + name + "\"");

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
                        return builder.toString();

                    i = matcher.end() - 1;
                }
                case '[' -> {
                    Matcher matcher = multilineString.matcher(builder);
                    if (matcher.find(i) && matcher.start() == i)
                        i = matcher.end() - 1;
                }
                case '-' -> {
                    if (i == builder.length() - 1)
                        return builder.toString();

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

        FiguraMod.debug("Script \"{}\" minified from {} chars to {} chars using LIGHT mode", name, script.length(), builder.length());

        error = false;
        return builder.toString();
    }

    private static String aggressiveMinify(String name, String script) {
        String start = regexMinify(name, script);
        if (error) return start;
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
                    int matchStart;
                    if (matcher.find(i) && (matchStart = matcher.start()) == i) {
                        builder.delete(i, matcher.end());
                        if (matchStart > 0 && matchStart + 1 < builder.length())
                            if (nameOops.matcher(builder.substring(matchStart - 1, matchStart + 1)).matches())
                                builder.insert(i, " ");
                            else
                                i --;
                    }
                }
                default -> {
                    Matcher word = words.matcher(builder);
                    if (word.find(i) && word.start() == i)
                        i = word.end() - 1;
                }
            }
        }

        FiguraMod.debug("Script \"{}\" minified from {} chars to {} chars using HEAVY mode", name, script.length(), builder.length());
        return builder.toString();
    }
    
    private static String ASTMinify(String name, String script) {
        try {
            Chunk chunk = new LuaParser(new StringReader(script)).Chunk();
            chunk.accept(new NameResolver());
            LuaScriptBuilderVisitor visitor = new LuaScriptBuilderVisitor();
            chunk.accept(visitor);
            error = false;
            FiguraMod.debug("Script \"{}\" minified from {} chars to {} chars using AST mode", name, script.length(), visitor.length());

            return visitor.getString();
        } catch (ParseException e) {
            return script;
        }
    }
}
