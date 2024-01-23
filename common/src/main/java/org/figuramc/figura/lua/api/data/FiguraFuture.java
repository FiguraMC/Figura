package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;

@LuaWhitelist
@LuaTypeDoc(value = "future", name = "Future")
public class FiguraFuture<T> {
    private boolean isDone;
    private boolean hasError;
    private LuaError errorObject;
    private T value;

    public void handle(T value, Throwable error) {
        if (error != null) error(error);
        else complete(value);
    }

    public void complete(T value) {
        if (!isDone) {
            this.value = value;
            isDone = true;
        }
    }

    public void error(Throwable t) {
        if (!isDone) {
            hasError = true;
            isDone = true;
            errorObject = t instanceof LuaError e ? e : new LuaError(t);
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

    @LuaMethodDoc(
            value = "future.has_error",
            overloads = @LuaMethodOverload(
                    returnType = Boolean.class
            )
    )
    public boolean hasError() {
        return hasError;
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

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.get_or_error",
            overloads = @LuaMethodOverload(
                    returnType = Object.class
            )
    )
    public T getOrError() {
        if (errorObject != null) throw errorObject;
        return value;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "future.throw_error"
    )
    public void throwError() {
        if (errorObject != null) throw errorObject;
    }

    @Override
    public String toString() {
        return "Future(isDone=%s)".formatted(isDone);
    }
}
