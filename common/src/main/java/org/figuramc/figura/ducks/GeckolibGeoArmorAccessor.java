package org.figuramc.figura.ducks;

import org.figuramc.figura.avatar.Avatar;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.geckolib.cache.model.GeoBone;

public interface GeckolibGeoArmorAccessor {
    Avatar figura$getAvatar();

    void figura$setEntityRenderTranslations(Matrix4f matrix4f);
    void figura$setModelRenderTranslations(Matrix4f matrix4f);

    float figura$getScaleWidth();
    float figura$getScaleHeight();

    
    GeoBone figura$getHeadBone();
    GeoBone figura$getLeftLegBone();
    GeoBone figura$getRightLegBone();
    GeoBone figura$getLeftArmBone();
    GeoBone figura$getRightArmBone();
    GeoBone figura$getBodyBone();
    GeoBone figura$getLeftBootBone();
    GeoBone figura$getRightBootBone();
}
