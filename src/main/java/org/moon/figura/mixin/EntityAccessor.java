package org.moon.figura.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Intrinsic
    @Invoker("getPermissionLevel")
    int getPermissionLevel();
}
