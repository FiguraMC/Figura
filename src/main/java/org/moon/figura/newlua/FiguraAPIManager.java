package org.moon.figura.newlua;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.util.*;
import java.util.function.Supplier;

/**
 * A set of Globals of which there is only one in the MC instance.
 * This set of Globals is used to compile and run other scripts.
 */
public class FiguraAPIManager {

    public static final Globals MOD_WIDE_GLOBALS;

    /**
     * Addon mods simply need to add their classes to the WHITELISTED_CLASSES set,
     * and whichever global vars they want to set into the API_GETTERS map.
     */
    public static final Set<Class<?>> WHITELISTED_CLASSES = new HashSet<>() {{

    }};

    public static final HashMap<String, Supplier<Object>> API_GETTERS = new LinkedHashMap<>() {{

    }};

    static {
        MOD_WIDE_GLOBALS = new Globals();
        MOD_WIDE_GLOBALS.load(new JseBaseLib());
        MOD_WIDE_GLOBALS.load(new PackageLib());
        MOD_WIDE_GLOBALS.load(new StringLib());
        MOD_WIDE_GLOBALS.load(new JseMathLib());

        LoadState.install(MOD_WIDE_GLOBALS);
        LuaC.install(MOD_WIDE_GLOBALS);

        LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
    }

    public static void setupTypesAndAPIs(FiguraLuaRuntime runtime) {
        for (Class<?> clazz : WHITELISTED_CLASSES)
            runtime.registerClass(clazz);
        for (Map.Entry<String, Supplier<Object>> entry : API_GETTERS.entrySet())
            runtime.setGlobal(entry.getKey(), entry.getValue().get());
    }

    static class ReadOnlyLuaTable extends LuaTable {
        public ReadOnlyLuaTable(LuaValue table) {
            presize(table.length(), 0);
            for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table
                    .next(n.arg1())) {
                LuaValue key = n.arg1();
                LuaValue value = n.arg(2);
                super.rawset(key, value.istable() ? new ReadOnlyLuaTable(value) : value);
            }
        }
        public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
        public void set(int key, LuaValue value) { error("table is read-only"); }
        public void rawset(int key, LuaValue value) { error("table is read-only"); }
        public void rawset(LuaValue key, LuaValue value) { error("table is read-only"); }
        public LuaValue remove(int pos) { return error("table is read-only"); }
    }

}
