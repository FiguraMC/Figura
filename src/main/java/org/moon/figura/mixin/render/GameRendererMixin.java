package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.ducks.GameRendererAccessor;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.model.rendering.texture.EntityRenderMode;
import org.moon.figura.trust.Trust;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private PostChain postEffect;
    @Shadow private boolean effectActive;

    @Unique
    private boolean avatarPostShader = false;
    @Unique
    private Avatar avatar;

    @Shadow protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);
    @Shadow protected abstract void loadEffect(ResourceLocation id);
    @Shadow public abstract void checkEntityPostEffect(Entity entity);

    @Shadow private float fov;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.BEFORE))
    private void onCameraRotation(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (avatar == null || avatar.luaRuntime == null || avatar.trust.get(Trust.VANILLA_MODEL_EDIT) == 0)
            return;

        float z = 0f;

        FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
        if (rot != null)
            z = (float) rot.z;

        FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null)
            z += (float) offset.z;

        matrix.mulPose(Vector3f.ZP.rotationDegrees(z));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || avatar.luaRuntime == null || avatar.trust.get(Trust.VANILLA_MODEL_EDIT) == 0) {
            if (avatarPostShader) {
                avatarPostShader = false;
                this.checkEntityPostEffect(entity);
            }
            return;
        }

        ResourceLocation resource = avatar.luaRuntime.renderer.postShader;
        if (resource == null) {
            if (avatarPostShader) {
                avatarPostShader = false;
                this.checkEntityPostEffect(entity);
            }
            return;
        }

        try {
            avatarPostShader = true;
            this.effectActive = true;
            if (this.postEffect == null || !this.postEffect.getName().equals(resource.toString()))
                this.loadEffect(resource);
        } catch (Exception ignored) {
            this.effectActive = false;
        }
    }

    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
    private void checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if (avatarPostShader)
            ci.cancel();
    }

    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;", shift = At.Shift.BEFORE),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"),
                    to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
            ))
    private void preRenderItemInHand(PoseStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        if (this.minecraft.player == null || !this.minecraft.options.getCameraType().isFirstPerson()) {
            avatar = null;
            return;
        }

        avatar = AvatarManager.getAvatarForPlayer(this.minecraft.player.getUUID());
        if (avatar != null) {
            avatar.renderMode = EntityRenderMode.FIRST_PERSON;
            avatar.renderEvent(tickDelta);
        }
    }

    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
    private void posRenderItemInHand(PoseStack matrices, Camera camera, float tickDelta, CallbackInfo ci) {
        if (avatar != null)
            avatar.postRenderEvent(tickDelta);
    }

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void tickFov(CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        if (avatar != null && avatar.luaRuntime != null && avatar.trust.get(Trust.VANILLA_MODEL_EDIT) == 1) {
            Float fov = avatar.luaRuntime.renderer.fov;
            if (fov != null) this.fov = fov;
        }
    }

    @Override @Intrinsic
    public double figura$getFov(Camera camera, float tickDelta, boolean changingFov) {
        return this.getFov(camera, tickDelta, changingFov);
    }
}
