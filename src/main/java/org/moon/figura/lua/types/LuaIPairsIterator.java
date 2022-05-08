package org.moon.figura.lua.types;

import org.moon.figura.lua.LuaWhitelist;

@LuaWhitelist
public class LuaIPairsIterator<T> extends LuaPairsIterator<T, Integer> {

    public LuaIPairsIterator(Class<T> typeClass) {
        super((t, i) -> i+1, typeClass, Integer.class);
    }
}
