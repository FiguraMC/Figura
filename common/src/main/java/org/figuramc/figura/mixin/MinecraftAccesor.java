package org.figuramc.figura.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccesor {
    @Invoker("getTickTargetMillis")
    float figura$invokeGetTickTargetMillis(float f);
}
