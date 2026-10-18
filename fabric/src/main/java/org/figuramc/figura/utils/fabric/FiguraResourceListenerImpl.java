package org.figuramc.figura.utils.fabric;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraResourceListener;

import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FiguraResourceListenerImpl extends FiguraResourceListener implements PreparableReloadListener {
    public FiguraResourceListenerImpl(String id, Consumer<ResourceManager> reloadConsumer) {
        super(id, reloadConsumer);
    }

    public static FiguraResourceListener createResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
        return new FiguraResourceListenerImpl(id, reloadConsumer);
    }

    public Identifier getFabricId() {
        return new FiguraIdentifier(this.id());
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier barrier, Executor applyExecutor) {
        return barrier.wait(sharedState.resourceManager()).thenAcceptAsync(reloadConsumer(), applyExecutor);
    }
}
