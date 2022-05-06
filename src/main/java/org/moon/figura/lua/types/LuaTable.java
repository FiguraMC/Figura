package org.moon.figura.lua.types;

import org.moon.figura.lua.LuaWhitelist;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaState;

import java.util.HashMap;
import java.util.Map;

/**
 * This object isn't pushed directly. Instead, its push() function is called,
 * which pushes an actual table to Lua.
 */
@LuaWhitelist
public class LuaTable {

    private final Map<Object, Object> map;

    public LuaTable() {
        map = new HashMap<>();
    }

    public LuaTable put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public LuaTable put(Integer key, Object value) {
        map.put(key, value);
        return this;
    }

    public void push(LuaState luaState) {
        luaState.newTable();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof JavaFunction func)
                luaState.pushJavaFunction(func);
            else
                luaState.pushJavaObject(entry.getValue());

            if (entry.getKey() instanceof Integer val)
                luaState.rawSet(-2, val);
            else
                luaState.setField(-2, String.valueOf(entry.getKey()));
        }
    }

}
