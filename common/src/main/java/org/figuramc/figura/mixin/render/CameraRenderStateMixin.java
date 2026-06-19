package org.figuramc.figura.mixin.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CameraRenderState.class)
public class CameraRenderStateMixin implements CameraRenderStateExtension {

    @Unique
    boolean figura$renderingNameTag = false;

    @Unique
    private Avatar figura$avatar = null;

    @Override
    public void figura$setAvatar(Avatar avatar) {
        this.figura$avatar = avatar;
    }

    @Override
    public Avatar figura$getAvatar() {
        return figura$avatar;
    }

    @Override
    public boolean figura$isRenderingNameTag() {
        return figura$renderingNameTag;
    }

    @Override
    public void figura$setRenderingNameTag(boolean rendering) {
        this.figura$renderingNameTag = rendering;
    }


}
