package org.figuramc.figura.lua.transfer;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaTypeManager;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

public class AvatarVarsTransformer implements LuaTransformer {
    private final Avatar source;
    private final Avatar consumer;
    private final LuaTypeManager targetTypes;

    public AvatarVarsTransformer(Avatar source, Avatar consumer) {
        this.source = source;
        this.consumer = consumer;
        targetTypes = consumer.luaRuntime.typeManager;
    }

    @Override
    public @NotNull LuaValue userdata(LuaUserdata usr) {
        return targetTypes.wrap(usr.m_instance);
    }

    @Override
    public @NotNull LuaValue function(LuaFunction f) {
        if (source.equals(consumer)) return f;
        return new InterAvatarFunctionWrapper(source, consumer, f);
    }

    @Override
    public @NotNull LuaValue table(LuaTable t) {
        return TransformerLuaTable.make(t, this).removeMeta();
    }
}
