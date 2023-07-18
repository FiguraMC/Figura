package org.figuramc.figura.lua.api.ping;

import org.figuramc.figura.lua.LuaWhitelist;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
<<<<<<< HEAD:src/main/java/org/moon/figura/lua/api/ping/PingFunction.java
import org.moon.figura.avatar.Avatar;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.config.Configs;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaTypeDoc;
=======
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/lua/api/ping/PingFunction.java

@LuaWhitelist
@LuaTypeDoc(
        name = "PingFunction",
        value = "ping_function"
)
public class PingFunction extends LuaFunction {

    private final int id;
    private final Avatar owner;
    private final boolean isHost;
    public final LuaFunction func;

    public PingFunction(int id, Avatar owner, LuaFunction func) {
        this.id = id;
        this.owner = owner;
        this.isHost = owner.isHost;
        this.func = func;
    }

    @Override
    public LuaValue call() {
        invoke(NONE);
        return NIL;
    }

    @Override
    public LuaValue call(LuaValue arg) {
        invoke(arg);
        return NIL;
    }

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2) {
        invoke(arg1, arg2);
        return NIL;
    }

    @Override
    public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
        invoke(arg1, arg2, arg3);
        return NIL;
    }

    @Override
    public Varargs invoke(Varargs args) {
        if (!isHost)
            return NIL;

        boolean sync = Configs.SYNC_PINGS.value;
        byte[] data = new PingArg(args).toByteArray();

        NetworkStuff.sendPing(id, sync, data);
        if (!sync) owner.runPing(id, data);

        return NIL;
    }

    @Override
    public String toString() {
        return "PingFunction";
    }
}
