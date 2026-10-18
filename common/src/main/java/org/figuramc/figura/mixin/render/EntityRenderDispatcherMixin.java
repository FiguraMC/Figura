package org.figuramc.figura.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Shadow public Camera camera;

    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitFlame(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lorg/joml/Quaternionf;)V"))
    private void figura$submitFlame(SubmitNodeCollector collector, PoseStack pose, EntityRenderState state,
                                    Quaternionf rotation, Operation<Void> original) {
        Avatar avatar = AvatarManager.getAvatar(state);
        if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.renderFire)
            return;
        original.call(collector, pose, state,
                UIHelper.paperdoll ? Axis.YP.rotationDegrees(UIHelper.fireRot) : rotation);
    }
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private <E extends Entity, S extends EntityRenderState> void render(S entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        if (this.camera == null) {
            ci.cancel();
            return;
        }

        Integer entityId = ((FiguraEntityRenderStateExtension)entityRenderState).figura$getEntityId();
        if (entityId == null)
            return;
        var level = Minecraft.getInstance().level;
        if (level == null)
            return;
        Entity entity = level.getEntity(entityId);
        if (entity == null)
            return;
        Entity owner = entity.getFirstPassenger();
        if (owner == null)
            return;

        Avatar avatar = AvatarManager.getAvatar(owner);
        if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.renderVehicle)
            ci.cancel();
    }
}
