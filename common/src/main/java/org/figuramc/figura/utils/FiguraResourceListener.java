package org.figuramc.figura.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Objects;
import java.util.function.Consumer;

public class FiguraResourceListener {
    private final String id;
    private final Consumer<ResourceManager> reloadConsumer;
    protected FiguraResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
        this.id = id;
        this.reloadConsumer = reloadConsumer;
    }

    @ExpectPlatform
    public static FiguraResourceListener createResourceListener(String id, Consumer<ResourceManager> reloadConsumer) {
        throw new AssertionError();
    }

    public String id() {
        return id;
    }

    public Consumer<ResourceManager> reloadConsumer() {
        return reloadConsumer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FiguraResourceListener) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.reloadConsumer, that.reloadConsumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reloadConsumer);
    }

    @Override
    public String toString() {
        return "FiguraResourceListener[" +
                "id=" + id + ", " +
                "reloadConsumer=" + reloadConsumer + ']';
    }

}
