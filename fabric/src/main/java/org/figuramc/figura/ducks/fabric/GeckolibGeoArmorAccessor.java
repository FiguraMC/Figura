package org.figuramc.figura.ducks.fabric;

import net.minecraft.world.entity.EquipmentSlot;
import org.figuramc.figura.avatar.Avatar;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public interface GeckolibGeoArmorAccessor {
    Avatar figura$getAvatar();

    EquipmentSlot figura$getSlot();
    float figura$getScaleWidth();
    float figura$getScaleHeight();
    AnimatedGeoModel<?> figura$getAnimatedModelProvider();

}
