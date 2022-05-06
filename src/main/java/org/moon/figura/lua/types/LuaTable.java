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

    private final Map<String, Object> map;

    public LuaTable() {
        map = new HashMap<>();
    }

    public LuaTable put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public LuaTable put(int key, Object value) {
        map.put(String.valueOf(key), value);
        return this;
    }

    public void push(LuaState luaState) {
        luaState.newTable();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof JavaFunction func)
                luaState.pushJavaFunction(func);
            else
                luaState.pushJavaObject(entry.getValue());
            luaState.setField(-2, entry.getKey());
        }
    }

}
