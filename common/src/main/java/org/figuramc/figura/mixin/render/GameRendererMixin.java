package org.figuramc.figura.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GameRendererAccessor;
import org.figuramc.figura.gui.FiguraPortraitRenderer;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {

    @Shadow @Final
    private Minecraft minecraft;

    @Shadow private boolean effectActive;

    @Shadow protected abstract float getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow public abstract void checkEntityPostEffect(Entity entity);

    @Shadow protected abstract void bobHurt(PoseStack poseStack, float f);

    @Shadow protected abstract void bobView(PoseStack poseStack, float f);

    @Shadow public abstract Minecraft getMinecraft();


    @Shadow @Final private Camera mainCamera;
    @Shadow @Nullable
    private ResourceLocation postEffectId;

    @Shadow @Final private CrossFrameResourcePool resourcePool;
    @Shadow private float fovModifier;
    @Shadow private float spinningEffectTime;
    @Shadow private float spinningEffectSpeed;
    @Shadow @Final private GuiRenderer guiRenderer;
    @Unique
    private boolean avatarPostShader = false;
    @Unique
    private boolean hasShaders;

    @WrapOperation(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lorg/joml/Matrix4f;rotation(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;"))
    private Matrix4f onCameraRotation(Matrix4f instance, Quaternionfc quat, Operation<Matrix4f> original) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar)) {
            original.call(instance, quat);
            return instance;
        }

        float z = 0f;

        FiguraVec3 rot = avatar.luaRuntime.renderer.cameraRot;
        if (rot != null)
            z = (float) rot.z;

        FiguraVec3 offset = avatar.luaRuntime.renderer.cameraOffsetRot;
        if (offset != null)
            z += (float) offset.z;

        instance.rotate(Axis.ZP.rotationDegrees(z));

        FiguraMat4 mat = avatar.luaRuntime.renderer.cameraMat;
        if (mat != null)
            instance.set(mat.toMatrix4f());

        // part of the bobbing fix
        PoseStack stack = new PoseStack();
        stack.last().pose().set(instance);

        float tickDelta = this.mainCamera.getPartialTickTime();
        this.bobHurt(stack, tickDelta);
        if (this.minecraft.options.bobView().get()) {
            this.bobView(stack, tickDelta);
        }

        instance.set(stack.last().pose());

        float screenScaleConfig = this.minecraft.options.screenEffectScale().get().floatValue();
        float lerpedPortalIntensity = Mth.lerp(tickDelta, this.minecraft.player.oPortalEffectIntensity, this.minecraft.player.portalEffectIntensity);
        float nauseaBlendFactor = this.minecraft.player.getEffectBlendFactor(MobEffects.NAUSEA, tickDelta);
        float spinEffectValue = Math.max(lerpedPortalIntensity, nauseaBlendFactor) * screenScaleConfig * screenScaleConfig;
        if (spinEffectValue > 0.0F) {
            float scaleFactor = 5.0F / (spinEffectValue * spinEffectValue + 5.0F) - spinEffectValue * 0.04F;
            scaleFactor *= scaleFactor;
            Vector3f vector3f = new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
            float piOver180 = 0.017453292F;
            float rotationFactor = (this.spinningEffectTime + tickDelta * this.spinningEffectSpeed) * piOver180;
            instance.rotate(rotationFactor, vector3f);
            instance.scale(1.0F / scaleFactor, 1.0F, 1.0F);
            instance.rotate(-rotationFactor, vector3f);
        }

       // FiguraMat3 normal = avatar.luaRuntime.renderer.cameraNormal;
      //  if (normal != null)
      //      stack.last().normal().set(normal.toMatrix3f());
        instance.rotate(quat);
        return instance;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void render(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
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
            if (this.postEffectId == null || !this.postEffectId.equals(resource)) {
                PostChain postchain = this.minecraft.getShaderManager().getPostChain(resource, LevelTargetBundle.MAIN_TARGETS);
                if (postchain != null)
                    postchain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
            }
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
            if (fov != null) this.fovModifier = fov;
        }
    }

    @Inject(method = "pick(F)V", at = @At("RETURN"))
    private void pick(float tickDelta, CallbackInfo ci) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("extendedPick");
        FiguraMod.extendedPickEntity = EntityUtils.getViewedEntity(32);
        FiguraMod.popProfiler(2);
    }

    @Override @Intrinsic
    public double figura$getFov(Camera camera, float tickDelta, boolean changingFov) {
        return this.getFov(camera, tickDelta, changingFov);
    }

    // bobbing fix courtesy of Iris; https://github.com/IrisShaders/Iris/blob/1.20.1/src/main/java/net/irisshaders/iris/mixin/MixinModelViewBobbing.java
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        hasShaders = ClientAPI.hasShaderPack();
    }

    @ModifyArg(method = "renderLevel", index = 0,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private PoseStack renderLevelBobHurt(PoseStack stack) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar) || hasShaders) return stack;
        stack.pushPose();
        stack.last().pose().identity();
        return stack;
    }

    @ModifyArg(method = "<init>", index = 4,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"))
    private List<PictureInPictureRenderer<?>> addPortraitRenderer(List<PictureInPictureRenderer<?>> list, @Local MultiBufferSource.BufferSource source) {
        List<PictureInPictureRenderer<?>> newList = new ArrayList<>(list);
        newList.add(new FiguraPortraitRenderer(source));
        return newList;
    }

    @WrapOperation(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void figura$stopBobView(GameRenderer instance, PoseStack stack, float f, Operation<GameRenderer> original) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar) || hasShaders)
            original.call(instance, stack, f);
    }

    @WrapOperation(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void figura$stopBobHurt(GameRenderer instance, PoseStack stack, float f, Operation<GameRenderer> original) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar) || hasShaders)
            original.call(instance, stack, f);
    }


    @WrapOperation(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private<T> T figura$disableConfusionOnMatrix(OptionInstance<T> instance, Operation<T> original) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        return (!RenderUtils.vanillaModelAndScript(avatar) || hasShaders) ? original.call(instance) : (T) (Object) 0.0;
    }

    @Override
    public GuiRenderer figura$getGuiRenderer() {
        return guiRenderer;
    }
}
