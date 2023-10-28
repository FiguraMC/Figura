package org.figuramc.figura.lua.api.data;

public class FiguraFuture<T> {
    private boolean isDone;
    private T value;
    public void complete(T value) {
        if (!isDone) {
            this.value = value;
            isDone = true;
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public T getValue() {
        return value;
    }
}
