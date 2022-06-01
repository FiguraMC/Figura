package org.moon.figura.utils;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Consumer;

public record FiguraResourceListener(String id, Consumer<ResourceManager> reloadConsumer) implements SimpleSynchronousResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return new FiguraIdentifier(id);
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        reloadConsumer.accept(manager);
    }
}
