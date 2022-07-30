package org.moon.figura.mixin.render;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Shadow private float xRot;
    @Shadow private float yRot;

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.BEFORE))
    private void setupRot(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
//        Avatar avatar = AvatarManager.getAvatar(focusedEntity);
//        if (avatar == null || avatar.luaState == null)
//            return;
//
//        float x = xRot;
//        float y = yRot;
//
//        FiguraVec3 rot = avatar.luaState.renderer.cameraRot;
//        if (rot != null) {
//            x = (float) rot.x;
//            y = (float) rot.y;
//        }
//
//        FiguraVec3 bonus = avatar.luaState.renderer.cameraBonusRot;
//        if (bonus != null) {
//            x += (float) bonus.x;
//            y += (float) bonus.y;
//        }
//
//        setRotation(y, x);
    }
}
