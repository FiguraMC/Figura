package org.figuramc.figura.ducks;

import org.figuramc.figura.avatar.Avatar;

public interface CameraRenderStateExtension {
    void figura$setAvatar(Avatar avatar);

    Avatar figura$getAvatar();

    boolean figura$isRenderingNameTag();

    void figura$setRenderingNameTag(boolean rendering);
}
