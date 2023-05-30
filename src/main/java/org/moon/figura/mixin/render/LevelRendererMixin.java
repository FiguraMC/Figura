package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Configs;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.rendering.EntityRenderMode;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;
    @Shadow @Final private RenderBuffers renderBuffers;
    @Shadow @Final private Minecraft minecraft;

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private Entity renderLevelRenderEntity(Entity entity) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null)
            avatar.renderMode = EntityRenderMode.RENDER;
        return entity;
    }

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource bufferSource, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar == null)
            return;

        if (bufferSource instanceof OutlineBufferSource outline && RenderUtils.vanillaModelAndScript(avatar) && avatar.luaRuntime.renderer.outlineColor != null) {
            int i = ColorUtils.rgbToInt(avatar.luaRuntime.renderer.outlineColor);
            outline.setColor(
                    i >> 16 & 0xFF,
                    i >> 8 & 0xFF,
                    i & 0xFF,
                    0xFF //does nothing :(
            );
        }

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(avatar);
        FiguraMod.pushProfiler("worldRender");

        avatar.worldRender(entity, cameraX, cameraY, cameraZ, matrices, bufferSource, entityRenderDispatcher.getPackedLightCoords(entity, tickDelta), tickDelta, EntityRenderMode.WORLD);

        FiguraMod.popProfiler(3);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0))
    private void renderLevelFirstPerson(PoseStack stack, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        if (camera.isDetached())
            return;

        Entity e = camera.getEntity();
        Avatar avatar = AvatarManager.getAvatar(e);

        if (avatar == null)
            return;

        //first person world parts
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        avatar.firstPersonWorldRender(e, bufferSource, stack, camera, tickDelta);

        //first person matrices
        if (!(e instanceof LivingEntity livingEntity) || !Configs.FIRST_PERSON_MATRICES.value)
            return;

        Avatar.firstPerson = true;
        stack.pushPose();

        EntityRenderer<? super LivingEntity> entityRenderer = this.entityRenderDispatcher.getRenderer(livingEntity);
        Vec3 offset = entityRenderer.getRenderOffset(livingEntity, tickDelta);
        Vec3 cam = camera.getPosition();

        stack.translate(
                Mth.lerp(tickDelta, livingEntity.xOld, livingEntity.getX()) - cam.x() + offset.x(),
                Mth.lerp(tickDelta, livingEntity.yOld, livingEntity.getY()) - cam.y() + offset.y(),
                Mth.lerp(tickDelta, livingEntity.zOld, livingEntity.getZ()) - cam.z() + offset.z()
        );

        float yaw = Mth.lerp(tickDelta, livingEntity.yRotO, livingEntity.getYRot());
        entityRenderer.render(livingEntity, yaw, tickDelta, stack, bufferSource, LightTexture.FULL_BRIGHT);

        stack.popPose();
        Avatar.firstPerson = false;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevel(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        AvatarManager.executeAll("worldRender", avatar -> avatar.render(tickDelta));
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void afterRenderLevel(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
        AvatarManager.executeAll("postWorldRender", avatar -> avatar.postWorldRenderEvent(tickDelta));
    }

    @ModifyArgs(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"))
    private void renderHitOutline(Args args) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return;

        args.set(6, (float) color.x);
        args.set(7, (float) color.y);
        args.set(8, (float) color.z);
        args.set(9, (float) color.w);
    }
}
