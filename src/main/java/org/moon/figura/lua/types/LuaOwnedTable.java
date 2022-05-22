package org.moon.figura.lua.types;

import org.terasology.jnlua.LuaState;

/**
 * A table that is owned by lua, and therefore takes up memory in lua.
 */
public class LuaOwnedTable<K> {

    private final LuaState state;
    private final String tableKey;

    /**
     * Only one LuaOwnedTable per LuaState can use a certain key.
     * @param key
     */
    public LuaOwnedTable(LuaState state, String key) {
        this.state = state;
        this.tableKey = key;
        state.newTable();
        state.setField(state.REGISTRYINDEX, key);
    }

    public void putValue(K key, Object value) {
        state.getField(state.REGISTRYINDEX, tableKey);
        state.pushJavaObject(key);
        state.pushJavaObject(value);
        state.setTable(-3);
        state.pop(1);
    }

    public <T> T getValue(K key, Class<T> clazz) {
        state.getField(state.REGISTRYINDEX, tableKey);
        state.pushJavaObject(key);
        state.getTable(-2);
        T obj = state.toJavaObject(-1, clazz);
        state.pop(2);
        return obj;
    }

}
