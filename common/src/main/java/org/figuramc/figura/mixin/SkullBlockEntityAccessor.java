package org.figuramc.figura.mixin;

import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {
    @Intrinsic
    @Accessor("profileCache")
    static GameProfileCache getProfileCache() {
        throw new AssertionError();
    }
}
