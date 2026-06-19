package org.figuramc.figura.utils.fabric;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraResourceListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class FiguraResourceListenerImpl extends FiguraResourceListener implements PreparableReloadListener {
    public FiguraResourceListenerImpl(String id, Consumer<ResourceManager> reloadConsumer) {
        super(id, reloadConsumer);
    }

    public static FiguraResourceListener createResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
        return new FiguraResourceListenerImpl(id, reloadConsumer);
    }

    public Identifier getId() {
        return FiguraIdentifier.of(this.id());
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor prepareExecutor, PreparationBarrier preparationBarrier, Executor applyExecutor) {
        ResourceManager manager = sharedState.resourceManager();
        return preparationBarrier.wait(null).thenRunAsync(() -> reloadConsumer().accept(manager), applyExecutor);
    }
}
