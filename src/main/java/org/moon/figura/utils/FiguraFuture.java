package org.moon.figura.utils;

import java.util.concurrent.CompletableFuture;

public class FiguraFuture {

    private CompletableFuture<Void> tasks;

    public void run(Runnable toRun) {
        if (tasks == null || tasks.isDone()) {
            tasks = CompletableFuture.runAsync(toRun);
        } else {
            tasks.thenRun(toRun);
        }
    }
}
