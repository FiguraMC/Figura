package org.figuramc.figura.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.HashMap;
import java.util.Map;

public class ReadOnlyLuaTable extends LuaTable {
    private final Map<LuaValue, LuaValue> seen;
    private final LuaTypeManager manager;

    public ReadOnlyLuaTable(LuaValue value) {
        this(value, null);
    }

    public ReadOnlyLuaTable(LuaValue table, LuaTypeManager manager) {
        this(table, new HashMap<>(), manager);
    }

    public ReadOnlyLuaTable(LuaValue table, Map<LuaValue, LuaValue> seen, LuaTypeManager manager) {
        this.seen = seen;
        this.manager = manager;
        seen.put(table, this);
        presize(table.length(), 0);
        for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table.next(n.arg1())) {
            super.rawset(
                    wrapIfNecessary(n.arg(1)),
                    wrapIfNecessary(n.arg(2))
            );
        }
        LuaValue mt = table.getmetatable();
        if (mt instanceof LuaTable metaTable)
            setMetaTable(metaTable);
    }

    private LuaValue wrapIfNecessary(LuaValue value) {
        if (seen.containsKey(value))
            return seen.get(value);

        if (value instanceof ReadOnlyLuaTable)
            seen.put(value, value);
        else if (value.istable()) {
            seen.put(value, new ReadOnlyLuaTable(value, seen, manager));
        } else if (value.isuserdata()) {
            Object userdata = value.checkuserdata();
            seen.put(value, LuaValue.userdataOf(userdata instanceof LuaUserdata u ? u.m_instance : userdata).setmetatable(value.getmetatable()));
        } else {
            seen.put(value, value);
        }

        return seen.get(value);
    }

    public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
    public void setMetaTable(LuaValue metaTable) { super.setmetatable(metaTable); }
    public void set(int key, LuaValue value) { error("table is read-only"); }
    public void rawset(int key, LuaValue value) { error("table is read-only"); }
    public void rawset(LuaValue key, LuaValue value) { error("table is read-only"); }
    public LuaValue remove(int pos) { return error("table is read-only"); }
}
