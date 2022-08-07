package org.moon.figura.lua.api.ping;

import org.luaj.vm2.LuaFunction;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaTypeDoc;

import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "PingAPI",
        description = "ping"
)
public class PingAPI {

    private final HashMap<String, PingFunction> map = new HashMap<>();
    private final Avatar owner;

    public PingAPI(Avatar owner) {
        this.owner = owner;
    }

    public PingFunction get(String arg) {
        return map.get(arg);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return get(arg);
    }

    @LuaWhitelist
    public void __newindex(String key, LuaFunction value) {
        PingFunction func = new PingFunction(key, owner, value);
        map.put(key, func);
    }

    @Override
    public String toString() {
        return "PingAPI";
    }
}
