package org.moon.figura.parsers;

import com.google.common.base.Splitter;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

//parses a lua script file
//and return a nbt list of it
public class LuaScriptParser {

    public static ListTag parse(String script) {
        ListTag nbt = new ListTag();

        for (String substring : Splitter.fixedLength(60000).split(script))
            nbt.add(StringTag.valueOf(substring));

        return nbt;
    }

    //TODO
    private static String minify(String script) {
        return script;
    }
}
