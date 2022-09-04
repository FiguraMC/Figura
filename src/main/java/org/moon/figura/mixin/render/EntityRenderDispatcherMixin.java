package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.trust.TrustContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "renderFlame", at = @At("HEAD"), cancellable = true)
    private void renderFlame(PoseStack stack, MultiBufferSource multiBufferSource, Entity entity, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || avatar.luaRuntime == null || avatar.trust.get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        if (!avatar.luaRuntime.renderer.renderFire)
            ci.cancel();
    }

    @ModifyVariable(method = "renderShadow", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private static float modifyShadowSize(float h, PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float g, LevelReader levelReader) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null && avatar.luaRuntime != null && avatar.luaRuntime.renderer.shadowRadius != null && avatar.trust.get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 1)
            return avatar.luaRuntime.renderer.shadowRadius;
        return h;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void render(E entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        Entity owner = entity.getControllingPassenger();
        if (owner == null)
            return;

        Avatar avatar = AvatarManager.getAvatar(owner);
        if (avatar == null || avatar.luaRuntime == null || avatar.trust.get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        if (!avatar.luaRuntime.renderer.renderVehicle)
            ci.cancel();
    }
}
