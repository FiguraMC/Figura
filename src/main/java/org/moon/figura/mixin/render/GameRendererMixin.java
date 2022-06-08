package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.math.vector.FiguraVec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.BEFORE))
    private void onCameraRotation(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (avatar == null || avatar.luaState == null)
            return;

        float z = 0f;

        FiguraVec3 rot = avatar.luaState.renderer.cameraRot;
        if (rot != null)
            z = (float) rot.z;

        FiguraVec3 bonus = avatar.luaState.renderer.cameraBonusRot;
        if (bonus != null)
            z += (float) bonus.z;

        matrix.mulPose(Vector3f.ZP.rotationDegrees(z));
    }
}
