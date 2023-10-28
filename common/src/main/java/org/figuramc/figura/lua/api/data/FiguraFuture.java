package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(value = "future", name = "Future")
public class FiguraFuture<T> {
    private boolean isDone;
    private T value;
    public void complete(T value) {
        if (!isDone) {
            this.value = value;
            isDone = true;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.is_done",
            overloads = @LuaMethodOverload(
                    returnType = Boolean.class
            )
    )
    public boolean isDone() {
        return isDone;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.get_value",
            overloads = @LuaMethodOverload(
                    returnType = Object.class
            )
    )
    public T getValue() {
        return value;
    }
}
