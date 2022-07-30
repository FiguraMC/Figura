package org.moon.figura.newlua;

import net.minecraft.world.entity.player.Player;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.moon.figura.math.newmatrix.FiguraMat2;
import org.moon.figura.math.newmatrix.FiguraMat3;
import org.moon.figura.math.newmatrix.FiguraMat4;
import org.moon.figura.math.newvector.*;
import org.moon.figura.newlua.api.entity.EntityAPI;
import org.moon.figura.newlua.api.entity.LivingEntityAPI;
import org.moon.figura.newlua.api.entity.PlayerAPI;
import org.moon.figura.newlua.api.event.EventsAPI;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A set of Globals of which there is only one in the MC instance.
 * This set of Globals is used to compile and run other scripts.
 */
public class FiguraAPIManager {

    /**
     * Addon mods simply need to add their classes to the WHITELISTED_CLASSES set,
     * and whichever global vars they want to set into the API_GETTERS map.
     */
    public static final Set<Class<?>> WHITELISTED_CLASSES = new HashSet<>() {{
        add(FiguraVec2.class);
        add(FiguraVec3.class);
        add(FiguraVec4.class);
        add(FiguraVec5.class);
        add(FiguraVec6.class);

        add(FiguraMat2.class);
        add(FiguraMat3.class);
        add(FiguraMat4.class);

        add(EntityAPI.class);
        add(LivingEntityAPI.class);
        add(PlayerAPI.class);

        add(EventsAPI.class);
        add(EventsAPI.LuaEvent.class);
    }};

    public static final Map<String, Function<FiguraLuaRuntime, Object>> API_GETTERS = new LinkedHashMap<>() {{
        put("events", r -> r.events = new EventsAPI());
    }};

    static {
        LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
    }

    public static void setupTypesAndAPIs(FiguraLuaRuntime runtime) {
        for (Class<?> clazz : WHITELISTED_CLASSES)
            runtime.registerClass(clazz);
        for (Map.Entry<String, Function<FiguraLuaRuntime, Object>> entry : API_GETTERS.entrySet())
            runtime.setGlobal(entry.getKey(), entry.getValue().apply(runtime));
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
