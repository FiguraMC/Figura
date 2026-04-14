package org.figuramc.figura.mixin.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.FiguraSkullAvatarAssociationExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin implements FiguraSkullAvatarAssociationExtension {
    @Unique
    private Avatar figura$avatar = null;

    @Override
    public Avatar figura$getAvatar() {
        return figura$avatar;
    }

    @Override
    public void figura$setAvatar(Avatar avatar) {
        this.figura$avatar = avatar;
    }
}
