package org.figuramc.figura.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Intrinsic
    @Accessor
    Level getLevel();

    @Intrinsic
    @Invoker("getPermissionLevel")
    int getPermissionLevel();
}
