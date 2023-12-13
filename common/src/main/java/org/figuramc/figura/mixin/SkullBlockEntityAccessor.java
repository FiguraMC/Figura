package org.figuramc.figura.mixin;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {
    @Intrinsic
    @Accessor("profileCache")
    static LoadingCache<String, CompletableFuture<Optional<GameProfile>>> getProfileCache() {
        throw new AssertionError();
    }
}
