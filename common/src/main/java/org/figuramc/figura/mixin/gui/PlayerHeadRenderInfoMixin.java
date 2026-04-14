package org.figuramc.figura.mixin.gui;

import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraSkullAvatarAssociationExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerSkinRenderCache.RenderInfo.class)
public abstract class PlayerHeadRenderInfoMixin implements FiguraSkullAvatarAssociationExtension {
    @Shadow
    public abstract RenderType renderType();

    // note that both this class, the PlayerSkinRenderCache.RenderInfo
    //                       and, the inner RenderType renderType field
    // implement FiguraSkullAvatarAssociationExtension
    // in this implementation, we prioritize the outer container (which is the RenderInfo)
    @Unique
    private FiguraSkullAvatarAssociationExtension figura$renderTypeAsAssociation() {
        return (FiguraSkullAvatarAssociationExtension)renderType();
    }

    @Unique
    private Avatar figura$avatar = null;

    // when getting the avatar, first check the outer container, then the inner renderType()
    @Override
    public Avatar figura$getAvatar() {
        return figura$avatar != null ? figura$avatar : figura$renderTypeAsAssociation().figura$getAvatar();
    }

    // when setting the avatar, overwrite both the outer container and the inner renderType()
    @Override
    public void figura$setAvatar(Avatar avatar) {
        figura$avatar = avatar;
        figura$renderTypeAsAssociation().figura$setAvatar(avatar);
    }
}
