package org.figuramc.figura.mixin;

import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerModelType.class)
public interface PlayerModelTypeAccessor {
    @Accessor("legacyServicesId")
    String figura$getLegacyServicesId();
}
