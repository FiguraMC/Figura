package org.figuramc.figura.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class ReadOnlyLuaTable extends LuaTable {
    public ReadOnlyLuaTable(LuaValue table) {
        presize(table.length(), 0);
        for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table
                .next(n.arg1())) {
            LuaValue key = n.arg1();
            LuaValue value = n.arg(2);
            super.rawset(key, value.istable() ? value == table ? this : new ReadOnlyLuaTable(value) : value);
        }
    }
    public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
    public void set(int key, LuaValue value) { error("table is read-only"); }
    public void rawset(int key, LuaValue value) { error("table is read-only"); }
    public void rawset(LuaValue key, LuaValue value) { error("table is read-only"); }
    public LuaValue remove(int pos) { return error("table is read-only"); }
}
