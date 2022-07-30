package org.moon.figura.lua.api;

import org.moon.figura.FiguraMod;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.types.LuaFunction;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;

import java.util.HashMap;
import java.util.UUID;

@LuaWhitelist
public class PingAPI {

    private final HashMap<String, PingFunction> table = new HashMap<>();
    private final boolean host;

    public PingAPI(UUID owner) {
        this.host = owner.compareTo(FiguraMod.getLocalPlayerUUID()) == 0;
    }

    @LuaWhitelist
    public static Object __index(@LuaNotNil PingAPI api, @LuaNotNil Object arg) {
        if (!(arg instanceof String))
            throw new LuaRuntimeException("Expected a String key, got " + arg + ".");

        PingFunction function = api.table.get(arg);
        return function == null ? null : function.function;
    }

    @LuaWhitelist
    public static void __newindex(@LuaNotNil PingAPI api, @LuaNotNil Object key, Object value) {
        if (key instanceof String s && value instanceof LuaFunction function) {
            PingFunction func = new PingFunction(s, function, api.host);
            api.table.put(s, func);
        } else {
            throw new LuaRuntimeException("Expected (String, Function) key pair, got (" + key + ", " + value + ").");
        }
    }

    @LuaWhitelist
    private static class PingFunction {

        private final String name;
        private final LuaFunction function;
        private final boolean host;

        private PingFunction(String name, LuaFunction function, boolean host) {
            this.name = name;
            this.function = function;
            this.host = host;
        }

        @LuaWhitelist
        public static void __call(PingFunction ping, LuaState luaState, Object... args) {
            if (!ping.host)
                return;

            boolean sync = true; //TODO - config
            NetworkManager.sendPing(ping.name, sync, args);

            if (sync)
                return;

            luaState.pushJavaObject(ping.function);
            luaState.pushJavaObject(args);
            luaState.call(args.length, 0);
        }
    }
}
