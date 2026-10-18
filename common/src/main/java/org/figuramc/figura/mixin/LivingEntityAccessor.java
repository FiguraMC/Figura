package org.figuramc.figura.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Intrinsic
    @Invoker("getModifiedSwingDuration")
    int getSwingDuration(SwingAnimation animation);

    @Accessor("swingState")
    LivingEntity.SwingState figura$getSwingState();

    @Mixin(LivingEntity.SwingState.class)
    interface SwingStateAccessor {
        @Accessor("ticks")
        int figura$getTicks();
    }

    @Intrinsic
    @Invoker("updateWalkAnimation")
    void invokeUpdateWalkAnimation(float distance);
}
