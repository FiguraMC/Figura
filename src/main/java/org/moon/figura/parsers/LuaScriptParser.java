package org.moon.figura.parsers;

import net.minecraft.nbt.ByteArrayTag;

import java.nio.charset.StandardCharsets;

public class LuaScriptParser {
  public ByteArrayTag parseScript(String script) {
    return new ByteArrayTag(
        // Deal with Lua Long Comments
        // Gotta deal with this first, else the regex has a chance of removing the end of a Long Comment
        removeLongComments(script)
            // Deal with short comments
            .replaceAll("--(?!\\[=*\\[).*?\\r?\\n", "\n")
            // Deal with extra whitespace after a newline
            .replaceAll("\\r\\n[\\t ]+", "\n")
            // Deal with extra padded whitespace everywhere else
            .replaceAll("[\\t ][\\t ]+", " ")
            .getBytes(StandardCharsets.UTF_8));
  }

  String removeLongComments(String string) {
    StringBuffer str = new StringBuffer(string);
    for (int i = 0; i < str.length();) {
      int delStart = i;
      if (str.charAt(i++) != '-')
        continue;
      if (str.charAt(i++) != '-')
        continue;
      if (str.charAt(i++) != '[')
        continue;
      int commentDepth = 0;
      while (str.charAt(i) == '=') {
        i++;
        commentDepth++;
      }
      if (str.charAt(i++) != '[')
        continue;
      int newlines = 0;
      while (i < str.length()) {
        if (str.charAt(i++) == ']') {
          int depth = 0;
          while (str.charAt(i) == '=') {
            i++;
            depth++;
          }
          if (depth == commentDepth && str.charAt(i) == ']') {
            str.delete(delStart, ++i);
            i = delStart;
            for (int o = 0; o < newlines; o++) {
              str.insert(i++, '\n');
            }
            break;
          }
        } else if (str.charAt(i) == '\n')
          // keep track of newlines within the long comment so that error lines remain
          // consistent
          newlines++;
      }
      if (i >= str.length())
        return string;// original script isnt formatted properly. return original.
    }
    return str.toString();
  }
}
