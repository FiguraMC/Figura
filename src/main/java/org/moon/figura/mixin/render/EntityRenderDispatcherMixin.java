package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "renderFlame", at = @At("HEAD"), cancellable = true)
    private void renderFlame(PoseStack stack, MultiBufferSource multiBufferSource, Entity entity, CallbackInfo ci) {
        if (UIHelper.forceNoFire) ci.cancel();

        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || avatar.luaState == null)
            return;

        Boolean fire = avatar.luaState.renderer.renderFireOverlay;
        if (fire != null && !fire)
            ci.cancel();
    }
}
