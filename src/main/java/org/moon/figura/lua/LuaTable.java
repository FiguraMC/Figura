package org.moon.figura.lua;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;

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

    public LuaTable add(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public LuaTable add(int key, Object value) {
        map.put(String.valueOf(key), value);
        return this;
    }

    public void push(LuaState luaState) {
        luaState.newTable();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            luaState.pushJavaObject(entry.getValue());
            luaState.setField(-2, entry.getKey());
        }
    }

}
