package org.moon.figura.parsers;

import net.minecraft.nbt.ByteArrayTag;

import java.nio.charset.StandardCharsets;

public class LuaScriptParser {
  public ByteArrayTag parseScript(String script) {
    StringBuilder string = new StringBuilder(script);
    for (int i = 0; i < string.length(); i++) {
      int parseStart = i;
      switch (string.charAt(i)) {
        // parse through single line strings and ignore them
        case '\'':
        case '"': {
          char stringChar = string.charAt(i++);
          while (i < string.length() && string.charAt(i) != stringChar) {
            if (string.charAt(i)=='\n')
              return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
            else if (string.charAt(i) == '\\')
              i++;
            i++;
          }
          break;
        }
        // parse through long multiline strings and ignore them
        case '[': {
          int end = parseLongString(string, i);
          if (end == -1)
            return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
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
          i++;
          if (i<string.length()&& string.charAt(i) == '[') {
            int end = parseLongString(string, i);
            // if long comment reaches until end of file, return original string
            if (end == -1) {
              return new ByteArrayTag(script.getBytes(StandardCharsets.UTF_8));
            }
            // if valid long comment not found, fall through to regular comment
            if (end != 0) {
              int newLines = 0;
              for (int o = parseStart; o < end; o++)
                if (string.charAt(o) == '\n')
                  newLines++;
              string.delete(parseStart, end + 1);
              string.insert(parseStart, "\n".repeat(newLines));
              i = parseStart + newLines-1;
              break;
            }
          }
          // parse comment until next newline
          while (i<string.length()&&string.charAt(i) != '\n')
            i++;
          string.delete(parseStart, i);
          i = parseStart - 1;
          break;
        }
        case ' ':
        case '\t':
        case '\r':
        case '\n': {
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

  int parseLongString(StringBuilder string, int startIndex) {
    if (string.charAt(startIndex) != '[')
      return 0;
    int i = startIndex;
    int commentDepth = 0;
    while (string.charAt(++i) == '=')
      commentDepth++;
    if (string.charAt(i) != '[')
      return 0;
    for (i++; i < string.length(); i++) {
      int parseStart = i;
      if (string.charAt(i) == ']') {
        int depth = 0;
        while (string.charAt(++i) == '=')
          depth++;
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
