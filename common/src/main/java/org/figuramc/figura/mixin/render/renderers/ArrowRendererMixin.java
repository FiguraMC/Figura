package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.entity.EntityAPI;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowRenderer.class)
public abstract class ArrowRendererMixin<T extends AbstractArrow> extends EntityRenderer<T> {

    protected ArrowRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), method = "render(Lnet/minecraft/world/entity/projectile/AbstractArrow;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true)
    private void render(T abstractArrow, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        Entity owner = abstractArrow.getOwner();
        if (owner == null)
            return;

        Avatar avatar = AvatarManager.getAvatar(owner);
        if (avatar == null || avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 0)
            return;

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(avatar);
        FiguraMod.pushProfiler("arrowRender");

        FiguraMod.pushProfiler("event");
        boolean bool = avatar.arrowRenderEvent(tickDelta, EntityAPI.wrap(abstractArrow));

        FiguraMod.popPushProfiler("render");
        if (bool || avatar.renderArrow(poseStack, multiBufferSource, tickDelta, light)) {
            poseStack.popPose();
            ci.cancel();
        }

        FiguraMod.popProfiler(4);
    }
}
