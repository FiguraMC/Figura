package org.moon.figura.ducks;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityRendererAccessor<T extends LivingEntity> {
    ElytraModel<T> figura$getElytraModel();
}
