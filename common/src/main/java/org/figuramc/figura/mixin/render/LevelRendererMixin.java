package org.figuramc.figura.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow @Final private Minecraft minecraft;

    @ModifyArg(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private <S extends EntityRenderState> S renderLevelRenderEntity(S entityRenderState) {
        Avatar avatar = AvatarManager.getAvatar(entityRenderState);
        if (avatar != null)
            avatar.renderMode = EntityRenderMode.RENDER;
        return entityRenderState;
    }

    @Inject(method = "submitEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private <S extends EntityRenderState> void renderEntity(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci, @Local S entityRenderState) {
        Integer entityId = ((FiguraEntityRenderStateExtension)entityRenderState).figura$getEntityId();
        if (entityId == null) return;
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        float tickDelta = ((FiguraEntityRenderStateExtension)entityRenderState).figura$getTickDelta();

        Avatar av = AvatarManager.getAvatar(entityRenderState);

        if (av == null)
            return;


        NodeCollectorExtension collectorExt = (NodeCollectorExtension) submitNodeCollector;

        collectorExt.submitFiguraModel(av, entityRenderState, ((avatar, entityState, multiBufferSource) -> {

            if (multiBufferSource instanceof OutlineBufferSource outline && RenderUtils.vanillaModelAndScript(avatar) && avatar.luaRuntime.renderer.outlineColor != null) {
                int i = ColorUtils.rgbToInt(avatar.luaRuntime.renderer.outlineColor);
                outline.setColor(
                        i
                );
            }

            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(avatar);
            FiguraMod.pushProfiler("worldRender");
            Vec3 cameraPos = levelRenderState.cameraRenderState.pos;

            avatar.worldRender(entity, cameraPos.x(), cameraPos.y(), cameraPos.z(), poseStack, multiBufferSource, entityRenderDispatcher.getPackedLightCoords(entity, tickDelta), tickDelta, EntityRenderMode.WORLD);

            FiguraMod.popProfiler(3);

            return null;
        }));

    }

    // TODO: Neo does not boot, complains method must be static but it won't compile if it is, the hell?
    // method_62214 for Fabric, lambda$addMainPass$2 for Neo and lambda$addMainPass$1 for Lex

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        AvatarManager.executeAll("worldRender", avatar -> avatar.render(deltaTracker.getGameTimeDeltaPartialTick(false)));
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void afterRenderLevel(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean bl, Camera camera, Matrix4f matrix4f, Matrix4f matrix4f2, Matrix4f matrix4f3, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl2, CallbackInfo ci) {
        AvatarManager.executeAll("postWorldRender", avatar -> avatar.postWorldRenderEvent(deltaTracker.getGameTimeDeltaPartialTick(false)));
    }

    @ModifyArg(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShapeRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDI)V"), index = 6)
    private int renderHitOutline(int colorInt) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return colorInt;

        return ARGB.colorFromFloat((float) color.w, (float) color.x, (float) color.y, (float) color.z);
    }
}