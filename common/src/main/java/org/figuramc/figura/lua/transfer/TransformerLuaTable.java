package org.figuramc.figura.lua.transfer;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.IdentityHashMap;

public class TransformerLuaTable extends LuaTable {
    private static final IdentityHashMap<LuaTable, TransformerLuaTable> wip = new IdentityHashMap<>();

    public static TransformerLuaTable make(LuaTable source, LuaTransformer transform) {
        TransformerLuaTable inProgress = wip.get(source);
        if (inProgress != null) return inProgress;
        return new TransformerLuaTable(source, transform);
    }

    protected TransformerLuaTable(LuaTable source, LuaTransformer transform) {
        // This could be unsafe, but we're using it internally so it's probably fine (??)
        wip.put(source, this);
        try {
            presize(source.rawlen(), 0);
            super.setmetatable(transform.visit(source.getmetatable()));
            for (Varargs kv = source.next(NIL); !kv.isnil(1); kv = source.next(kv.arg1())) {
                LuaValue k = kv.arg(1);
                LuaValue v = kv.arg(2);
                super.rawset(transform.visit(k), transform.visit(v));
            }
        } finally {
            wip.remove(source);
        }
    }

    public TransformerLuaTable removeMeta() {
        super.setmetatable(null);
        return this;
    }
}
