package org.figuramc.figura.ducks;

import org.figuramc.figura.avatar.Avatar;

// used by:
// * PlayerHeadRenderInfoMixin -> PlayerSkinRenderCache.RenderInfo.class
// * SkullBlockRendererMixin -> SkullBlockRenderer.class
// * RenderTypeMixin -> RenderType.class
// associates an object (one of the above) with an Avatar
// used by the skull rendering system to figure out which Avatar goes with which player skull
public interface FiguraSkullAvatarAssociationExtension {
    Avatar figura$getAvatar();
    void figura$setAvatar(Avatar avatar);
}
