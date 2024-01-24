package org.figuramc.figura.ducks;

import org.figuramc.figura.avatar.Avatar;
import org.joml.Matrix4f;

public interface GeckolibGeoArmorAccessor {
    Avatar figura$getAvatar();

    void figura$setEntityRenderTranslations(Matrix4f matrix4f);
    void figura$setModelRenderTranslations(Matrix4f matrix4f);

    float figura$getScaleWidth();
    float figura$getScaleHeight();

}
