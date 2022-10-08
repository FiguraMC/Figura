package org.moon.figura.lua.api.ping;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaTypeDoc;

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

        boolean sync = Config.SYNC_PINGS.asBool();
        byte[] data = new PingArg(args).toByteArray();

        NetworkManager.sendPing(id, sync, data);
        if (!sync) owner.runPing(id, data);

        return NIL;
    }

    @Override
    public String toString() {
        return "PingFunction";
    }
}
