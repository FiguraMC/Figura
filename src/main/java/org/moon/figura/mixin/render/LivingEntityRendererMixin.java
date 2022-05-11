package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.config.Config;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    private Avatar currentAvatar;

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void preRender(LivingEntity entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        currentAvatar = AvatarManager.getAvatar(entity);
        if (currentAvatar == null)
            return;
        EntityModel<?> model = this.getModel();
        if (model instanceof PlayerModel<?> playerModel)
            if (currentAvatar.luaState != null)
                currentAvatar.luaState.vanillaModel.alterModel(playerModel);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void endRender(LivingEntity entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        if (currentAvatar == null)
            return;
        //Render avatar with params
        EntityModel<?> model = this.getModel();

        //When viewed 3rd person, render all non-world parts.
        //No camera/hud or whatever in yet. when they are, they won't be included here either.
        currentAvatar.renderer.currentFilterScheme = AvatarRenderer.RENDER_REGULAR;

        currentAvatar.onRender(entity, yaw, delta, matrices, bufferSource, light, model);
        if (model instanceof PlayerModel<?> playerModel)
            if (currentAvatar.luaState != null)
                currentAvatar.luaState.vanillaModel.restoreModel(playerModel);

        currentAvatar = null;
    }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void shouldShowName(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (UIHelper.forceNameplate)
            cir.setReturnValue((boolean) Config.PREVIEW_NAMEPLATE.value);
        else if (!Minecraft.renderNames())
            cir.setReturnValue(false);
        else if ((boolean) Config.SELF_NAMEPLATE.value && livingEntity == Minecraft.getInstance().player)
            cir.setReturnValue(true);
    }
}
