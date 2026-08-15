package org.figuramc.figura.lua.transfer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

/**
 * Visitor pattern for some Lua structures
 */
public interface LuaTransformer {
    default @NotNull LuaValue visit(LuaValue value) {
        if (value instanceof LuaUserdata usr) return userdata(usr);
        if (value instanceof LuaTable t) return table(t);
        if (value instanceof LuaFunction f) return function(f);
        LuaValue result = visitExtra(value);
        if (result != null) return result;
        return fallback(value);
    }

    /**
     * Override to implement additional types.
     */
    default @Nullable LuaValue visitExtra(LuaValue value) { return null; }

    default @NotNull LuaValue userdata(LuaUserdata usr) { return usr; }
    default @NotNull LuaValue table(LuaTable t) { return t; }
    default @NotNull LuaValue function(LuaFunction f) { return f; }
    default @NotNull LuaValue fallback(LuaValue any) { return any; }
}
