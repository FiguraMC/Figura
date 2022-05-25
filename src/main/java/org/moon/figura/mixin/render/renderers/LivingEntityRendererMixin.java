package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.model.rendering.AvatarRenderer;
import org.moon.figura.config.Config;
import org.moon.figura.mixin.render.elytra.ElytraLayerAccessor;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    private Avatar currentAvatar;


    @Final
    @Shadow
    protected List<RenderLayer<T, M>> layers;

    @Shadow protected abstract boolean isBodyVisible(T livingEntity);
    @Shadow protected abstract RenderType getRenderType(T livingEntity, boolean bl, boolean bl2, boolean bl3);

    public ElytraModel<T> getElytraModel() {
        if (!((Object) this instanceof PlayerRenderer)) return null;
        RenderLayer<T, M> layerCandidate = layers.get(6);
        if (!(layerCandidate instanceof ElytraLayer<T, M> elytraLayer)) { //a bit jank but it should get the elytra layer, look at PlayerRenderer.class
            FiguraMod.LOGGER.warn("Unable to find elytra layer... Seems some other mod is messing with the layers, or " + FiguraMod.MOD_ID + " version is weird.");
            return null;
        }
        return ((ElytraLayerAccessor<T, M>) elytraLayer).getElytraModel();
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void preRender(LivingEntity entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        currentAvatar = AvatarManager.getAvatar(entity);
        if (currentAvatar == null)
            return;

        if (currentAvatar.luaState != null) {

            if (getModel() instanceof PlayerModel<?> playerModel && entity instanceof Player) {
                currentAvatar.luaState.vanillaModel.copyPlayerModel(playerModel);
                if (TrustManager.get(entity.getUUID()).get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 1)
                    currentAvatar.luaState.vanillaModel.alterPlayerModel(playerModel);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    private void endRender(T entity, float yaw, float delta, PoseStack matrices, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        if (currentAvatar == null)
            return;

        boolean bodyVisible = this.isBodyVisible(entity);
        boolean translucent = !bodyVisible && Minecraft.getInstance().player != null && !entity.isInvisibleTo(Minecraft.getInstance().player);
        boolean visible = this.getRenderType(entity, bodyVisible, translucent, Minecraft.getInstance().shouldEntityAppearGlowing(entity)) != null;

        //Render avatar with params
        EntityModel<?> model = this.getModel();

        //When viewed 3rd person, render all non-world parts.
        //No camera/hud or whatever in yet. when they are, they won't be included here either.
        if (visible) {
            currentAvatar.renderer.currentFilterScheme = AvatarRenderer.RENDER_REGULAR;
            currentAvatar.onRender(entity, yaw, delta, translucent ? 0.15f : 1f, matrices, bufferSource, light, (LivingEntityRenderer<?, ?>) (Object) this, getElytraModel());
        }


        if (model instanceof PlayerModel<?> playerModel && entity instanceof Player)
            if (TrustManager.get(entity.getUUID()).get(TrustContainer.Trust.VANILLA_MODEL_EDIT) == 1)
                if (currentAvatar.luaState != null)
                    currentAvatar.luaState.vanillaModel.restorePlayerModel(playerModel);

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
