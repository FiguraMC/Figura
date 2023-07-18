package org.figuramc.figura.lua.api.ping;

import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.luaj.vm2.LuaFunction;
<<<<<<< HEAD:src/main/java/org/moon/figura/lua/api/ping/PingAPI.java
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaTypeDoc;
=======
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/lua/api/ping/PingAPI.java

import java.util.HashMap;

@LuaWhitelist
@LuaTypeDoc(
        name = "PingAPI",
        value = "pings"
)
public class PingAPI {

    private final HashMap<String, PingFunction> map = new HashMap<>();
    private final HashMap<Integer, String> idMap = new HashMap<>();
    private final Avatar owner;

    public PingAPI(Avatar owner) {
        this.owner = owner;
    }

    public PingFunction get(String arg) {
        return map.get(arg);
    }

    public String getName(int id) {
        return idMap.get(id);
    }

    @LuaWhitelist
    public Object __index(String arg) {
        return get(arg);
    }

    @LuaWhitelist
    public void __newindex(@LuaNotNil String key, LuaFunction value) {
        int id = (key.hashCode() + 1) * 31;
        if (value == null) {
            map.remove(key);
            idMap.remove(id);
        } else {
            PingFunction func = new PingFunction(id, owner, value);
            map.put(key, func);
            idMap.put(id, key);
        }
    }

    @Override
    public String toString() {
        return "PingAPI";
    }
}
