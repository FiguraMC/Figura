package org.moon.figura.parsers;

import net.minecraft.nbt.ByteArrayTag;
import org.moon.figura.config.Config;

import java.nio.charset.StandardCharsets;

public class LuaScriptParser {
    public ByteArrayTag parseScript(String script) {
        if (!Config.FORMAT_SCRIPT.asBool())
            return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
        StringBuilder string = new StringBuilder(script);
        for (int i = 0; i < string.length(); i++) {
            int parseStart = i;
            switch (string.charAt(i)) {
                // parse through single line strings and ignore them
                case '\'':
                case '"': {
                    char stringChar = string.charAt(i++);
                    while (i < string.length() && string.charAt(i) != stringChar) {
                        // should there be a newline, assume improper formatting.
                        if (string.charAt(i) == '\n')
                            return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
                        // increase i an extra time to ignore escaped characters.
                        else if (string.charAt(i) == '\\')
                            i++;
                        i++;
                    }
                    break;
                }
                // parse through long multiline strings and ignore them
                case '[': {
                    int end = parseLongString(string, i);
                    // should the long string run until the end of the file, assume improper
                    // formatting.
                    if (end == -1)
                        return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
                    // if this bracket isnt a long string
                    if (end == 0)
                        break;
                    i = end;
                    break;
                }
                // parse through comments
                case '-': {
                    if (++i >= string.length())
                        return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
                    if (string.charAt(i) != '-')
                        break;
                    // parse through long comments
                    if (++i < string.length() && string.charAt(i) == '[') {
                        int end = parseLongString(string, i);
                        // if long comment reaches until end of file, return original string
                        if (end == -1) {
                            return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
                        }
                        if (end != 0) {
                            // count newlines so that they can be reinserted
                            int newLines = 0;
                            for (int o = parseStart; o < end; o++)
                                if (string.charAt(o) == '\n')
                                    newLines++;
                            string.delete(parseStart, end + 1);
                            string.insert(parseStart, "\n".repeat(newLines));
                            i = parseStart + newLines - 1;
                            break;
                        }
                        // if valid long comment not found, fall through to regular comment
                    }
                    // parse comment until next newline
                    while (i < string.length() && string.charAt(i) != '\n')
                        i++;
                    string.delete(parseStart, i);
                    i = parseStart - 1;
                    break;
                }
                // parse whitespace and remove excess, while retaining newlines.
                case ' ':
                case '\t':
                case '\r':
                case '\n': {
                    // count newlines so that they can be reinserted
                    int newLines = 0;
                    while (true) {
                        if (i >= string.length())
                            break;
                        char c = string.charAt(i);
                        if (c == '\n')
                            newLines++;
                        if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
                            i++;
                        else
                            break;
                    }
                    string.delete(parseStart, i);
                    String insert = newLines > 0 ? "\n".repeat(newLines) : " ";
                    string.insert(parseStart, insert);
                    i = parseStart + insert.length() - 1;
                    break;
                }
            }
        }
        return new ByteArrayTag(string.toString().getBytes(StandardCharsets.UTF_8));
    }

    // parse a Lua long string/comment from a StringBuilder.
    // `startIndex` is expected to be the the index of the first left square bracket
    // returns 0 if there is no long string/comment,
    // returns -1 if the long string/comment reaches the end of file before closing.
    // otherwise, returns the index of the closing bracket.
    int parseLongString(StringBuilder string, int startIndex) {
        if (string.charAt(startIndex) != '[')
            return 0;
        int i = startIndex;
        int commentDepth = 0;
        while (++i < string.length() && string.charAt(i) == '=')
            commentDepth++;
        if (i >= string.length() || string.charAt(i) != '[')
            return 0;
        for (i++; i < string.length(); i++) {
            int parseStart = i;
            if (string.charAt(i) == ']') {
                int depth = 0;
                while (++i < string.length() && string.charAt(i) == '=')
                    depth++;
                if (i >= string.length())
                    return -1;
                if (string.charAt(i) != ']')
                    continue;
                if (depth == commentDepth)
                    return i;
                i = parseStart;
            }
        }
        return -1;
    }
}
