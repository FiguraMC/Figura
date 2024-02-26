package org.figuramc.figura.mixin.render;

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
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.figuramc.figura.utils.ColorUtils;
import org.figuramc.figura.utils.RenderUtils;
import org.joml.Matrix4f;
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
                    0xFF // does nothing :(
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

        // first person world parts
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        avatar.firstPersonWorldRender(e, bufferSource, stack, camera, tickDelta);

        // first person matrices
        if (!(e instanceof LivingEntity livingEntity) || !Configs.FIRST_PERSON_MATRICES.value)
            return;

        Avatar.firstPerson = true;

        int size = ((PoseStackAccessor)stack).getPoseStack().size();
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

        do {
            stack.popPose();
        } while(((PoseStackAccessor)stack).getPoseStack().size() > size);

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

    @ModifyArg(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"), index = 6)
    private float renderHitOutlineX(float x) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return x;

        return (float) color.x;
    }

    @ModifyArg(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"), index = 7)
    private float renderHitOutlineY(float y) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return y;

        return (float) color.y;
    }

    @ModifyArg(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"), index = 8)
    private float renderHitOutlineZ(float z) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return z;

        return (float) color.z;
    }

    @ModifyArg(method = "renderHitOutline", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"), index = 9)
    private float renderHitOutlineW(float w) {
        Avatar avatar = AvatarManager.getAvatar(this.minecraft.getCameraEntity());
        FiguraVec4 color;

        if (avatar == null || avatar.luaRuntime == null || (color = avatar.luaRuntime.renderer.blockOutlineColor) == null)
            return w;

        return (float) color.w;
    }
}