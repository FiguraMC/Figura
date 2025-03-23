package org.figuramc.figura.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.mixin.render.PoseStackAccessor;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixinFabric {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow @Final private RenderBuffers renderBuffers;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = {"method_62214"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0))
    private void renderLevelFirstPerson(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profilerFiller, Matrix4f matrix4f, Matrix4f matrix4f2, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, Frustum frustum, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci, @Local PoseStack stack) {
        if (camera.isDetached())
            return;

        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        Entity e = camera.getEntity();
        Avatar avatar = AvatarManager.getAvatar(e);

        if (avatar == null || !(e instanceof LivingEntity livingEntity))
            return;

        EntityRenderer<LivingEntity, LivingEntityRenderState> entityRenderer = (EntityRenderer<LivingEntity, LivingEntityRenderState>) this.entityRenderDispatcher.getRenderer(livingEntity);

        LivingEntityRenderState state = entityRenderer.createRenderState(livingEntity, deltaTracker.getGameTimeDeltaPartialTick(Minecraft.getInstance().level.tickRateManager().isEntityFrozen(e)));
        // first person world parts
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        avatar.firstPersonWorldRender(e, bufferSource, stack, camera, tickDelta);

        // first person matrices
        if (!Configs.FIRST_PERSON_MATRICES.value)
            return;

        Avatar.firstPerson = true;

        int lastIndex = ((PoseStackAccessor)stack).getLastIndex();
        stack.pushPose();

        Vec3 offset = entityRenderer.getRenderOffset(state);
        Vec3 cam = camera.getPosition();

        stack.translate(
                Mth.lerp(tickDelta, livingEntity.xOld, livingEntity.getX()) - cam.x() + offset.x(),
                Mth.lerp(tickDelta, livingEntity.yOld, livingEntity.getY()) - cam.y() + offset.y(),
                Mth.lerp(tickDelta, livingEntity.zOld, livingEntity.getZ()) - cam.z() + offset.z()
        );


        entityRenderer.render(state, stack, bufferSource, LightTexture.FULL_BRIGHT);

        do {
            stack.popPose();
        } while(((PoseStackAccessor)stack).getLastIndex() > lastIndex);

        Avatar.firstPerson = false;
    }


    @Inject(method =  {"method_62214"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"))
    public void applyFiguraNormals(FogParameters fogParameters, DeltaTracker deltaTracker, Camera camera, ProfilerFiller profilerFiller, Matrix4f matrix4f, Matrix4f matrix4f2, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, Frustum frustum, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci, @Local PoseStack poseStack) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity());
        if (!RenderUtils.vanillaModelAndScript(avatar)) return;

        FiguraMat3 normal = avatar.luaRuntime.renderer.cameraNormal;
        if (normal != null)
            poseStack.last().normal().set(normal.toMatrix3f());
    }
}
