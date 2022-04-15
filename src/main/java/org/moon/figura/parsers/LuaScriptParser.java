package org.moon.figura.parsers;

import com.google.common.base.Splitter;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

//parses a lua script file
//and return a nbt list of it
public class LuaScriptParser {

    public static NbtList parse(String script) {
        NbtList nbt = new NbtList();

        for (String substring : Splitter.fixedLength(60000).split(script))
            nbt.add(NbtString.of(substring));

        return nbt;
    }

    //TODO
    private static String minify(String script) {
        return script;
    }
}
