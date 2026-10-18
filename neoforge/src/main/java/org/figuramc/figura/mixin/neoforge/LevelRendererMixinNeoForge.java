package org.figuramc.figura.mixin.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.matrix.FiguraMat3;
import org.figuramc.figura.mixin.render.PoseStackAccessor;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixinNeoForge {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void figura$submitFirstPerson(PoseStack poseStack, LevelRenderState levelRenderState,
                                         SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Avatar cameraAvatar = AvatarManager.getAvatar(minecraft.getCameraEntity() == null ? minecraft.player : minecraft.getCameraEntity());
        if (RenderUtils.vanillaModelAndScript(cameraAvatar)) {
            FiguraMat3 normal = cameraAvatar.luaRuntime.renderer.cameraNormal;
            if (normal != null)
                poseStack.last().normal().set(normal.toMatrix3f());
        }

        Camera camera = minecraft.gameRenderer.mainCamera();
        if (camera.isDetached())
            return;

        DeltaTracker deltaTracker = minecraft.getDeltaTracker();
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        Entity entity = camera.entity();
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null || !(entity instanceof LivingEntity livingEntity))
            return;

        EntityRenderer<LivingEntity, LivingEntityRenderState> renderer =
                (EntityRenderer<LivingEntity, LivingEntityRenderState>) this.entityRenderDispatcher.getRenderer(livingEntity);
        LivingEntityRenderState state = renderer.createRenderState(livingEntity,
                deltaTracker.getGameTimeDeltaPartialTick(minecraft.level.tickRateManager().isEntityFrozen(entity)));

        avatar.firstPersonWorldRender(entity, submitNodeCollector, poseStack, camera, tickDelta);

        if (Configs.FIRST_PERSON_MATRICES.value) {
            Avatar.firstPerson = true;
            int lastIndex = ((PoseStackAccessor) poseStack).getLastIndex();
            poseStack.pushPose();
            Vec3 offset = renderer.getRenderOffset(state);
            Vec3 cameraPosition = camera.position();
            poseStack.translate(
                    Mth.lerp(tickDelta, livingEntity.xOld, livingEntity.getX()) - cameraPosition.x() + offset.x(),
                    Mth.lerp(tickDelta, livingEntity.yOld, livingEntity.getY()) - cameraPosition.y() + offset.y(),
                    Mth.lerp(tickDelta, livingEntity.zOld, livingEntity.getZ()) - cameraPosition.z() + offset.z()
            );

            renderer.submit(state, poseStack, submitNodeCollector, levelRenderState.cameraRenderState);
            do {
                poseStack.popPose();
            } while (((PoseStackAccessor) poseStack).getLastIndex() > lastIndex);
            Avatar.firstPerson = false;
        }
    }

}
