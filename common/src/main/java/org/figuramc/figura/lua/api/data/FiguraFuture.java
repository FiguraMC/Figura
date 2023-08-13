package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

@LuaWhitelist
@LuaTypeDoc(
        name = "Future",
        value = "future"
)
public class FiguraFuture<T> {
    private boolean isDone;
    private T value;
    public FiguraFuture() {}

    @LuaWhitelist
    @LuaMethodDoc("future.is_done")
    public boolean isDone() {
        return isDone;
    }

    @LuaWhitelist
    @LuaMethodDoc("future.get")
    public T get() {
        return value;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Future";
    }
}
