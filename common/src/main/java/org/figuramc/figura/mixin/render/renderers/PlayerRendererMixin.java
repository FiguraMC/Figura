package org.figuramc.figura.mixin.render.renderers;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.EntityRendererAccessor;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.figuramc.figura.ducks.NodeCollectorExtension;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;
import java.util.regex.Pattern;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> implements EntityRendererAccessor {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float shadowRadius) {
        super(context, entityModel, shadowRadius);
    }

    @Unique
    private Avatar avatar;

    @Unique
    boolean isNameRendering, hasScore;

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 1))
    private void enableModifyPlayerName(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        // render name
        FiguraMod.popPushProfiler("name");
        isNameRendering = true;
    }

    @Override
    public boolean figura$isRenderingName() {
        return isNameRendering;
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "TAIL"))
    private void disableModifyPlayerName(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        isNameRendering = false;
    }


    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 0))
    private void setHasScore(AvatarRenderState playerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        hasScore = playerRenderState.scoreText != null;
    }


    @Override
    public boolean figura$hasScore() {
        return hasScore;
    }

    @ModifyArg(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 1))
    private Component modifyPlayerNameText(Component text, @Local(argsOnly = true) AvatarRenderState player) {
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic)
            return text;

        // text
        Avatar avatar = AvatarManager.getAvatar(player);
        EntityNameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.ENTITY;

        // customization boolean, which also is the permission check
        boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;

        Component name = player.nameTag;
        FiguraMod.popPushProfiler("text");

        Component replacement = hasCustom && custom.getJson() != null ? custom.getJson().copy() : name;

        // name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        // badges
        FiguraMod.popPushProfiler("badges");
        if (Minecraft.getInstance().level.getEntity(player.id) != null) { // null while dead
			replacement = Badges.appendBadges(replacement, Minecraft.getInstance().level.getEntity(player.id).getUUID(), config > 1);
		}

        FiguraMod.popPushProfiler("applyName");
        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(player.nameTag.getString()) + "\\b", replacement);

        return text;
    }

    // Push for scoreboard rendering
    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
    private void pushProfilerForRender(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        FiguraMod.popPushProfiler("render");
        FiguraMod.pushProfiler("scoreboard");
    }

    // Pop the profiler after everything's done
    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "TAIL"))
    private void popProfiler(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        FiguraMod.popProfiler(5);
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void renderNameTag(AvatarRenderState playerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        Entity entity = Minecraft.getInstance().level.getEntity(playerRenderState.id);

        if (config == 0 || AvatarManager.panic || !(entity instanceof Player player) || this.entityRenderDispatcher.distanceToSqr(player) > 4096)
            return;

        // get customizations
        Avatar avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        EntityNameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.ENTITY;

        // customization boolean, which also is the permission check
        boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;
        if (custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 0) {
            avatar.noPermissions.add(Permissions.NAMEPLATE_EDIT);
        } else if (avatar != null) {
            avatar.noPermissions.remove(Permissions.NAMEPLATE_EDIT);
        }

        // enabled
        if (hasCustom && !custom.visible) {
            ci.cancel();
            return;
        }

        // If the user has an avatar equipped, figura nameplate rendering will be enabled so the profiler is pushed
        if (hasCustom) {
            FiguraMod.pushProfiler(FiguraMod.MOD_ID);
            FiguraMod.pushProfiler(player.getName().getString());
            FiguraMod.pushProfiler("nameplate");
        }
    }



    @Inject(at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"), method = "renderHand")
    private void onRenderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl, CallbackInfo ci) {
        avatar = AvatarManager.getAvatarForPlayer(Minecraft.getInstance().player.getUUID());


        Avatar localAvatar = avatar;
        BiFunction<MultiBufferSource, PoseStack, Boolean> lambda = (bufferSource, stack) -> {
            float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

            if (localAvatar != null && localAvatar.luaRuntime != null) {
                VanillaPart part = localAvatar.luaRuntime.vanilla_model.PLAYER;
                PlayerModel model = this.getModel();

                part.save(model);

                if (localAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                    part.preTransform(model);
                    part.posTransform(model);
                }
            }

            return true;
        };

        ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPreRenderingCallback(lambda);
        ((FiguraSubmitCallBackExtension)(Object)modelPart).figura$addPostRenderingCallback(() -> {
            if (localAvatar != null && localAvatar.luaRuntime != null)
                localAvatar.luaRuntime.vanilla_model.PLAYER.restore(model);
            }
        );
    }

    @Inject(at = @At("RETURN"), method = "renderHand")
    private void postRenderHand(PoseStack stack, SubmitNodeCollector submitNodeCollector, int light, ResourceLocation resourceLocation, ModelPart arm, boolean bl, CallbackInfo ci) {
        if (avatar == null)
            return;

        NodeCollectorExtension nodeCollectorExt = (NodeCollectorExtension) submitNodeCollector;

        float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        PoseStack copy = new PoseStack();
        copy.pushPose(); // save the current stack
        copy.last().set(stack.last());

        PlayerModel playerModel = getModel();

        nodeCollectorExt.submitFiguraModel(avatar, null, (playerAvatar, state, bufferSource) -> {
            if (playerAvatar != null && playerAvatar.luaRuntime != null) {
                VanillaPart part = playerAvatar.luaRuntime.vanilla_model.PLAYER;
                PlayerModel model = this.getModel();

                part.save(model);

                if (playerAvatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                    part.preTransform(model);
                    part.posTransform(model);
                }
            }

            playerAvatar.firstPersonRender(copy, bufferSource, Minecraft.getInstance().player, playerModel, arm, light, delta);

            if (playerAvatar.luaRuntime != null)
                playerAvatar.luaRuntime.vanilla_model.PLAYER.restore(playerModel);

            return null;
        });

        avatar = null;
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("HEAD"), cancellable = true)
    private void setupRotations(AvatarRenderState playerRenderState, PoseStack matrices, float f, float g, CallbackInfo cir) {
        Avatar avatar = AvatarManager.getAvatar(playerRenderState);
        if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.getRootRotationAllowed()) {
            cir.cancel();
        }
    }
}
