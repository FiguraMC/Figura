package org.figuramc.figura.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.figuramc.figura.ducks.ArmorStandAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin implements ArmorStandAccessor {
    @Shadow protected abstract void setMarker(boolean bl);

    @Override
    public void figura$setMarker(boolean isMarker) {
        setMarker(isMarker);
    }
}
