package org.figuramc.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GameRendererAccessor;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {

    @Shadow @Final Minecraft minecraft;
    @Shadow PostChain postEffect;
    @Shadow private boolean effectActive;
    @Shadow private float fov;

    @Shadow protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);
    @Shadow abstract void loadEffect(ResourceLocation id);
    @Shadow public abstract void checkEntityPostEffect(Entity entity);

    @Unique
    private boolean avatarPostShader = false;
    @Unique
    private Matrix4f bobbingMatrix;
    @Unique
    private boolean hasShaders;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.BEFORE))
    private void onCameraRotation(float tickDelta, long limitTime, PoseStack stack, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar))
            return;

        float z = 0f;

        FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
        if (rot != null)
            z = (float) rot.z;

        FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null)
            z += (float) offset.z;

        stack.mulPose(Axis.ZP.rotationDegrees(z));

        FiguraMat4 mat = avatar.luaRuntime.renderer.cameraMat;
        if (mat != null)
            stack.last().pose().set(mat.toMatrix4f());

        FiguraMat3 normal = avatar.luaRuntime.renderer.cameraNormal;
        if (normal != null)
            stack.last().normal().set(normal.toMatrix3f());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void render(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        Entity entity = this.minecraft.getCameraEntity();
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (!RenderUtils.vanillaModelAndScript(avatar)) {
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
            avatar.luaRuntime.renderer.postShader = null;
        }
    }

    @Inject(method = "checkEntityPostEffect", at = @At("HEAD"), cancellable = true)
    private void checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if (avatarPostShader)
            ci.cancel();
    }

    @Inject(method = "tickFov", at = @At("RETURN"))
    private void tickFov(CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            Float fov = avatar.luaRuntime.renderer.fov;
            if (fov != null) this.fov = fov;
        }
    }

    @Inject(method = "pick", at = @At("RETURN"))
    private void pick(float tickDelta, CallbackInfo ci) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("extendedPick");
        FiguraMod.extendedPickEntity = EntityUtils.getViewedEntity(32);
        FiguraMod.popProfiler(2);
    }

    // bobbing fix courtesy of Iris; https://github.com/IrisShaders/Iris/blob/1.20/src/main/java/net/coderbot/iris/mixin/MixinModelViewBobbing.java
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(float tickDelta, long limitTime, PoseStack stack, CallbackInfo ci) {
        hasShaders = ClientAPI.hasShaderPack();
    }

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"), index = 0)
    private PoseStack renderLevelBobHurt(PoseStack stack) {
        if (hasShaders) return stack;

        stack.pushPose();
        stack.last().pose().identity();

        return stack;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;", shift = At.Shift.BEFORE),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V")
            ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void renderLevelSaveBobbing(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci, boolean bl, Camera camera, Entity entity, PoseStack poseStack, double d) {
        if (hasShaders) return;
        bobbingMatrix = new Matrix4f(poseStack.last().pose());
        poseStack.popPose();
    }

    // Optifine slightly changes the locals here, adds an extra boolean
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;", shift = At.Shift.BEFORE),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V")
            ), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void renderLevelSaveBobbingOF(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci, boolean bl, boolean bl2, Camera camera, Entity entity, PoseStack poseStack, double d) {
        if (hasShaders) return;
        bobbingMatrix = new Matrix4f(poseStack.last().pose());
        poseStack.popPose();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetProjectionMatrix(Lorg/joml/Matrix4f;)V"))
    private void renderLevelResetProjectionMatrix(float tickDelta, long limitTime, PoseStack matrix, CallbackInfo ci) {
        if (hasShaders) return;
        matrix.last().pose().mul(bobbingMatrix);
        bobbingMatrix = null;
    }

    @Override @Intrinsic
    public double figura$getFov(Camera camera, float tickDelta, boolean changingFov) {
        return this.getFov(camera, tickDelta, changingFov);
    }
}
