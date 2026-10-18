package org.figuramc.figura.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.GameRendererAccessor;
import org.figuramc.figura.gui.FiguraPortraitRenderer;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements GameRendererAccessor {

    @Shadow @Final
    private Minecraft minecraft;

    @Shadow @Final private Camera mainCamera;
    @Shadow @Final private GuiRenderer guiRenderer;
    @Shadow @Final private GameRenderState gameRenderState;
    @Shadow @Final private List<Identifier> failedPostEffects;
    @Unique
    private boolean hasShaders;

    @Inject(method = "extract", at = @At("RETURN"))
    private void render(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        if (!gameRenderState.shouldRenderLevel)
            return;
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar))
            return;
        Identifier resource = avatar.luaRuntime.renderer.postShader;
        if (resource == null)
            return;
        if (failedPostEffects.contains(resource)) {
            avatar.luaRuntime.renderer.postShader = null;
            return;
        }
        if (!gameRenderState.requestedPostEffects.contains(resource))
            gameRenderState.requestedPostEffects.addFirst(resource);
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void pick(DeltaTracker deltaTracker, CallbackInfo ci) {
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("extendedPick");
        FiguraMod.extendedPickEntity = EntityUtils.getViewedEntity(32);
        FiguraMod.popProfiler(2);
    }

    @Override @Intrinsic
    public double figura$getFov(Camera camera, float tickDelta, boolean changingFov) {
        return camera.getFov();
    }

    // bobbing fix courtesy of Iris; https://github.com/IrisShaders/Iris/blob/1.20.1/src/main/java/net/irisshaders/iris/mixin/MixinModelViewBobbing.java
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(CallbackInfo ci) {
        hasShaders = ClientAPI.hasShaderPack();
    }

    @ModifyArg(method = "<init>", index = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"))
    private List<PictureInPictureRenderer<?>> addPortraitRenderer(List<PictureInPictureRenderer<?>> list) {
        // NeoForge registers renderer factories through its own event.
        if (org.figuramc.figura.utils.PlatformUtils.isModLoaded("neoforge"))
            return list;
        List<PictureInPictureRenderer<?>> newList = new ArrayList<>(list);
        newList.add(new FiguraPortraitRenderer());
        newList.add(new org.figuramc.figura.gui.FiguraGui.HudRenderer());
        return newList;
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"))
    private GpuBufferSlice figura$moveBobbingToView(
            ProjectionMatrixBuffer buffer, Matrix4f projection, Operation<GpuBufferSlice> original) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar) || hasShaders)
            return original.call(buffer, projection);

        CameraRenderState cameraState = gameRenderState.levelRenderState.cameraRenderState;
        Matrix4f vanillaView = mainCamera.getViewRotationMatrix(new Matrix4f());
        Matrix4f effects = new Matrix4f(cameraState.projectionMatrix).invert().mul(projection);
        // Preserve custom camera * bob/hurt/nausea * vanilla camera order without distorting the projection.
        cameraState.viewRotationMatrix.mul(new Matrix4f(vanillaView).invert()).mul(effects).mul(vanillaView);
        return original.call(buffer, cameraState.projectionMatrix);
    }

    @Override
    public GuiRenderer figura$getGuiRenderer() {
        return guiRenderer;
    }
}
