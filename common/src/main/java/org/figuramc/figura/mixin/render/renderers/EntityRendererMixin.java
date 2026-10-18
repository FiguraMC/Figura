package org.figuramc.figura.mixin.render.renderers;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.utils.RenderUtils;
import java.util.regex.Pattern;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.ducks.EntityRendererAccessor;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.figuramc.figura.permissions.Permissions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> implements EntityRendererAccessor {

    @ModifyExpressionValue(method = "extractShadow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getShadowRadius(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)F"))
    private float figura$shadowRadius(float radius, @Local(argsOnly = true) S state) {
        Avatar avatar = AvatarManager.getAvatar(state);
        if (RenderUtils.vanillaModelAndScript(avatar) && avatar.luaRuntime.renderer.shadowRadius != null)
            return avatar.luaRuntime.renderer.shadowRadius;
        return radius;
    }

    @Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
    private void shouldRender(T entity, Frustum frustum, double d, double e, double f, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null && avatar.permissions.get(Permissions.OFFSCREEN_RENDERING) == 1)
            cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "extractRenderState")
    private void extractRenderState(T entity, S entityRenderState, float f, CallbackInfo ci) {
        ((FiguraEntityRenderStateExtension)entityRenderState).figura$setEntityId(entity.getId());
        ((FiguraEntityRenderStateExtension)entityRenderState).figura$setTickDelta(f);
    }


    @ModifyArg(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V", ordinal = 0), index = 6)
    private CameraRenderState setAvatarForScoreSubmission(CameraRenderState cameraRenderState, @Local(argsOnly = true) S entityRenderState) {
        Avatar avatar = AvatarManager.getAvatar(entityRenderState);
        if (avatar == null)
            return cameraRenderState;
        CameraRenderState replacement = new CameraRenderState();
        replacement.pos = cameraRenderState.pos;
        replacement.blockPos = cameraRenderState.blockPos;
        replacement.initialized = cameraRenderState.initialized;
        replacement.orientation = cameraRenderState.orientation;
        ((CameraRenderStateExtension) replacement).figura$setAvatar(avatar);
        ((CameraRenderStateExtension) replacement).figura$setRenderingNameTag(false);
        return replacement;
    }
    @ModifyArg(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V", ordinal = 1), index = 6)
    private CameraRenderState setAvatarForNameSubmission(CameraRenderState cameraRenderState, @Local(argsOnly = true) S entityRenderState) {
        Avatar avatar = AvatarManager.getAvatar(entityRenderState);
        if (avatar == null)
            return cameraRenderState;
        CameraRenderState replacement = new CameraRenderState();
        replacement.pos = cameraRenderState.pos;
        replacement.blockPos = cameraRenderState.blockPos;
        replacement.initialized = cameraRenderState.initialized;
        replacement.orientation = cameraRenderState.orientation;
        ((CameraRenderStateExtension) replacement).figura$setAvatar(avatar);
        ((CameraRenderStateExtension) replacement).figura$setRenderingNameTag(true);
        return replacement;
    }
    @ModifyArg(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V", ordinal = 1), index = 3)
    private Component modifyPlayerNameText(Component text, @Local(argsOnly = true) S state) {
        if (!(state instanceof AvatarRenderState player))
            return text;
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic)
            return text;

        // text
        Avatar avatar = AvatarManager.getAvatar(player);
        EntityNameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.ENTITY;

        // customization boolean, which also is the permission check
        boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;

        Component name = player.nameTag;

        Component replacement = hasCustom && custom.getJson() != null ? custom.getJson().copy() : name;

        // name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        // badges
        if (Minecraft.getInstance().level.getEntity(player.id) != null) { // null while dead
			replacement = Badges.appendBadges(replacement, Minecraft.getInstance().level.getEntity(player.id).getUUID(), config > 1);
		}
        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(player.nameTag.getString()) + "\\b", replacement);

        return text;
    }

}
