package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.LivingEntityRendererAccessor;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.gui.PopupMenu;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.figuramc.figura.model.rendering.PartFilterScheme;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.ui.UIHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<S>> extends EntityRenderer<T,S> implements RenderLayerParent<S, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow @Final protected List<RenderLayer<S, M>> layers;


    @Shadow
    public static int getOverlayCoords(LivingEntityRenderState arg, float whiteOverlayProgress) {
        return 0;
    }

    @Shadow protected abstract float getWhiteOverlayProgress(S arg);

    @Shadow protected abstract boolean isBodyVisible(S livingEntityRenderState);

    @Shadow @Final protected ItemModelResolver itemModelResolver;

    @Shadow
    public abstract @NotNull M getModel();

    @Unique
    private Avatar currentAvatar;
    @Unique
    private Matrix4f lastPose;

    @Inject(at = @At("HEAD"), method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    private void onRender(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        currentAvatar = AvatarManager.getAvatar(livingEntityRenderState);
        if (currentAvatar == null)
            return;

        lastPose = poseStack.last().pose();
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            ),
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            index = 5
    )
    private int customOverlay(int thing) {
        return LivingEntityRendererAccessor.overrideOverlay.orElse(thing);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", shift = At.Shift.BEFORE), method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", cancellable = true)
    private void setFiguraCallbacks(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (currentAvatar == null)
            return;

        Avatar localAvatar = currentAvatar;
        M model = getModel();

        // so basically set the figura callbacks up before the model is submitted
        FiguraSubmitCallBackExtension submitCallBackExtension = (FiguraSubmitCallBackExtension) model;
        submitCallBackExtension.figura$addPreRenderingCallback((bufferSource, pose) -> {
            // transform parts so render layers see figura changes
            // do pos/rot/scale, then visibility
            figura$transformParts(localAvatar, model);
            figura$doPosTransform(localAvatar, model);
            return true;
        });

        submitCallBackExtension.figura$addPostRenderingCallback(() -> {
            // Restore transform
            if (localAvatar.luaRuntime != null)
                localAvatar.luaRuntime.vanilla_model.PLAYER.restore(getModel());
        });
    }

    // then submit the figura model once vanilla has set posing up
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V", shift = At.Shift.AFTER), method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", cancellable = true)
    private void submitFiguraModel(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (currentAvatar == null)
            return;

        Avatar localAvatar = currentAvatar;

        M model = getModel();
        
        if (Avatar.firstPerson) {
            localAvatar.updateMatrices(model, poseStack);
            currentAvatar = null;
            lastPose = null;
            poseStack.popPose();
            ci.cancel();
            return;
        }

        boolean showBody = isBodyVisible(livingEntityRenderState);
        boolean translucent = !showBody && !livingEntityRenderState.isInvisibleToPlayer;
        boolean glowing = !showBody && livingEntityRenderState.appearsGlowing();
        boolean invisible = !translucent && !showBody && !glowing;
        Integer id = livingEntityRenderState instanceof AvatarRenderState playerRenderState ? playerRenderState.id : ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getEntityId();
        if (id == null) return;

        var modelState = RenderUtils.captureModelState(model);

        Integer entityIdBoxed = ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getEntityId();
        if (entityIdBoxed == null) return;
        int entityId = entityIdBoxed;
        float tickDelta = ((FiguraEntityRenderStateExtension)livingEntityRenderState).figura$getTickDelta();

        int overlay = getOverlayCoords(livingEntityRenderState, getWhiteOverlayProgress(livingEntityRenderState));

        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        // actually do the render here

        NodeCollectorExtension nodeCollectorExtension = (NodeCollectorExtension) submitNodeCollector;

        Matrix4f lastPs = new Matrix4f(lastPose);
        PoseStack poseStack2 = new PoseStack();
        poseStack2.pushPose();
        poseStack2.last().set(poseStack.last());

        nodeCollectorExtension.submitFiguraModel(localAvatar, livingEntityRenderState, ((avatar, livingEntityState, bufferSource) -> {
            // When viewed 3rd person, render all non-world parts.
            RenderUtils.restoreModelPoseState(model, modelState);

            PartFilterScheme filter = invisible ? PartFilterScheme.PIVOTS : PartFilterScheme.MODEL;

            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(avatar);

            FiguraMod.pushProfiler("calculateMatrix");
            Matrix4f diff = new Matrix4f(lastPs).invert().mul(poseStack2.last().pose());
            FiguraMat4 poseMatrix = new FiguraMat4().set(diff);

            FiguraMod.popPushProfiler("render");

            figura$transformParts(avatar, model);

            FiguraMod.popPushProfiler("renderEvent");
            avatar.renderEvent(tickDelta, poseMatrix);

            avatar.render(entity, livingEntityState.yRot, tickDelta, translucent ? 0.15f : 1f, poseStack2, bufferSource, livingEntityState.lightCoords, overlay, model, filter, translucent, glowing);

            FiguraMod.popPushProfiler("postRenderEvent");
            avatar.postRenderEvent(tickDelta, poseMatrix);

            FiguraMod.popProfiler(3);

            // undo transformations
            if (avatar.luaRuntime != null)
                avatar.luaRuntime.vanilla_model.PLAYER.restore(model);
            return null;
        }));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
    private void nullifyAvatar(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (currentAvatar == null)
            return;

        currentAvatar = null;
        lastPose = null;
    }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void shouldShowName(T livingEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        if (UIHelper.paperdoll)
            cir.setReturnValue(Configs.PREVIEW_NAMEPLATE.value);
        else if (!Minecraft.renderNames() || livingEntity.getUUID().equals(PopupMenu.getEntityId()))
            cir.setReturnValue(false);
        else if (!AvatarManager.panic) {
            if (Configs.SELF_NAMEPLATE.value && livingEntity == Minecraft.getInstance().player)
                cir.setReturnValue(true);
            else if (Configs.NAMEPLATE_RENDER.value == 2 || (Configs.NAMEPLATE_RENDER.value == 1 && livingEntity != FiguraMod.extendedPickEntity))
                cir.setReturnValue(false);
        }
    }

    @Unique
    void figura$transformParts(Avatar currentAvatar, M model) {
        if (currentAvatar.luaRuntime != null) {
            VanillaPart part = currentAvatar.luaRuntime.vanilla_model.PLAYER;
            part.save(model);
            if (currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                // this one basically does transformations such as rotation
                part.preTransform(model);
            }
        }
    }

    @Unique
    void figura$doPosTransform(Avatar currentAvatar, M model) {
        // this is responsible for visibility stuff
        if (currentAvatar.luaRuntime != null && currentAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1)
            currentAvatar.luaRuntime.vanilla_model.PLAYER.posTransform(model);
    }

    // Add the skull item back in after it being cleared
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;clear()V", ordinal = 0, shift = At.Shift.AFTER))
    private void shouldShowName(T entity, S livingEntityRenderState, float f, CallbackInfo ci) {
       this.itemModelResolver.updateForLiving(livingEntityRenderState.headItem, entity.getItemBySlot(EquipmentSlot.HEAD), ItemDisplayContext.HEAD, entity);
    }

    @Inject(method = "isEntityUpsideDown", at = @At("HEAD"), cancellable = true)
    private static void isEntityUpsideDown(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (RenderUtils.vanillaModelAndScript(avatar)) {
            Boolean upsideDown = avatar.luaRuntime.renderer.upsideDown;
            if (upsideDown != null)
                cir.setReturnValue(upsideDown);
        }
    }
}
