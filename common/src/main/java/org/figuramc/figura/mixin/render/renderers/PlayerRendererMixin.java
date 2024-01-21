package org.figuramc.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.*;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.compat.SimpleVCCompat;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.lua.api.vanilla_model.VanillaPart;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.RenderUtils;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float shadowRadius) {
        super(context, entityModel, shadowRadius);
    }

    @Unique
    private Avatar avatar;

    @Inject(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void renderNameTag(AbstractClientPlayer player, Component text, PoseStack stack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        // return on config or high entity distance
        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic || this.entityRenderDispatcher.distanceToSqr(player) > 4096)
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

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(player.getName().getString());
        FiguraMod.pushProfiler("nameplate");

        stack.pushPose();

        // pivot
        FiguraMod.pushProfiler("pivot");
        FiguraVec3 pivot;
        if (hasCustom && custom.getPivot() != null)
            pivot = custom.getPivot();
        else
            pivot = FiguraVec3.of(0f, player.getBbHeight() + 0.5f, 0f);

        stack.translate(pivot.x, pivot.y, pivot.z);

        // rotation
        stack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // pos
        FiguraMod.popPushProfiler("position");
        if (hasCustom && custom.getPos() != null) {
            FiguraVec3 pos = custom.getPos();
            stack.translate(pos.x, pos.y, pos.z);
        }

        // scale
        FiguraMod.popPushProfiler("scale");
        float scale = 0.025f;
        FiguraVec3 scaleVec = FiguraVec3.of(-scale, -scale, scale);
        if (hasCustom && custom.getScale() != null)
            scaleVec.multiply(custom.getScale());

        stack.scale((float) scaleVec.x, (float) scaleVec.y, (float) scaleVec.z);

        // text
        Component name = Component.literal(player.getName().getString());
        FiguraMod.popPushProfiler("text");
        Component replacement = hasCustom && custom.getJson() != null ? custom.getJson().copy() : name;

        // name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        // badges
        FiguraMod.popPushProfiler("badges");
        replacement = Badges.appendBadges(replacement, player.getUUID(), config > 1);

        FiguraMod.popPushProfiler("applyName");
        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(player.getName().getString()) + "\\b", replacement);

        // * variables * // 
        FiguraMod.popPushProfiler("colors");
        boolean notSneaking = !player.isDiscrete();
        boolean deadmau = text.getString().equals("deadmau5");

        int bgColor = hasCustom && custom.background != null ? custom.background : (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 0xFF) << 24;
        int outlineColor = hasCustom && custom.outlineColor != null ? custom.outlineColor : 0x202020;

        boolean outline = hasCustom && custom.outline;
        boolean shadow = hasCustom && custom.shadow;

        light = hasCustom && custom.light != null ? custom.light : light;

        Font font = this.getFont();
        Matrix4f matrix4f = stack.last().pose();
        Matrix4f textMatrix = matrix4f;
        if (shadow) {
            stack.pushPose();
            stack.scale(1, 1, -1);
            textMatrix = stack.last().pose();
            stack.popPose();
        }

        // render scoreboard
        FiguraMod.popPushProfiler("render");
        FiguraMod.pushProfiler("scoreboard");
        boolean hasScore = false;
        if (this.entityRenderDispatcher.distanceToSqr(player) < 100) {
            // get scoreboard
            Scoreboard scoreboard = player.getScoreboard();
            Objective scoreboardObjective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (scoreboardObjective != null) {
                hasScore = true;

                // render scoreboard
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(player, scoreboardObjective);

                Component text1 = Component.literal(Integer.toString(score.get())).append(" ").append(scoreboardObjective.getDisplayName());
                float x = -font.width(text1) / 2f;
                float y = deadmau ? -10f : 0f;

                font.drawInBatch(text1, x, y, 0x20FFFFFF, false, matrix4f, multiBufferSource, notSneaking ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, bgColor, light);
                if (notSneaking) {
                    if (outline)
                        font.drawInBatch8xOutline(text1.getVisualOrderText(), x, y, -1, outlineColor, matrix4f, multiBufferSource, light);
                    else
                        font.drawInBatch(text1, x, y, -1, shadow, textMatrix, multiBufferSource, Font.DisplayMode.NORMAL, 0, light);
                }
            }
        }

        // render name
        FiguraMod.popPushProfiler("name");
        List<Component> textList = TextUtils.splitText(text, "\n");

        for (int i = 0; i < textList.size(); i++) {
            Component text1 = textList.get(i);

            if (text1.getString().isEmpty())
                continue;

            int line = i - textList.size() + (hasScore ? 0 : 1);

            float x = -font.width(text1) / 2f;
            float y = (deadmau ? -10f : 0f) + (font.lineHeight + 1) * line;

            font.drawInBatch(text1, x, y, 0x20FFFFFF, false, matrix4f, multiBufferSource, notSneaking ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, bgColor, light);
            if (notSneaking) {
                if (outline)
                    font.drawInBatch8xOutline(text1.getVisualOrderText(), x, y, -1, outlineColor, matrix4f, multiBufferSource, light);
                else
                    font.drawInBatch(text1, x, y, -1, shadow, textMatrix, multiBufferSource, Font.DisplayMode.NORMAL, 0, light);
            }

            // Renders Simple VC icons at the end of the nameplate
            if (ClientAPI.isModLoaded("voicechat") && textList.get(i) == textList.get(textList.size()-1))
                SimpleVCCompat.renderSimpleVCIcon(player, text1, stack, multiBufferSource, light);
        }

        FiguraMod.popProfiler(5);
        stack.popPose();
        ci.cancel();
    }

    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/model/PlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"), method = "renderHand")
    private void onRenderHand(PoseStack stack, MultiBufferSource multiBufferSource, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        if (avatar != null && avatar.luaRuntime != null) {
            VanillaPart part = avatar.luaRuntime.vanilla_model.PLAYER;
            PlayerModel<AbstractClientPlayer> model = this.getModel();

            part.save(model);

            if (avatar.permissions.get(Permissions.VANILLA_MODEL_EDIT) == 1) {
                part.preTransform(model);
                part.posTransform(model);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "renderHand")
    private void postRenderHand(PoseStack stack, MultiBufferSource multiBufferSource, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (avatar == null)
            return;

        float delta = Minecraft.getInstance().getFrameTime();
        avatar.firstPersonRender(stack, multiBufferSource, player, (PlayerRenderer) (Object) this, arm, light, delta);

        if (avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.PLAYER.restore(this.getModel());

        avatar = null;
    }

    @Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true)
    private void setupRotations(AbstractClientPlayer entity, PoseStack poseStack, float f, float f2, float f3, CallbackInfo cir) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (RenderUtils.vanillaModelAndScript(avatar) && !avatar.luaRuntime.renderer.getRootRotationAllowed()) {
            cir.cancel();
        }
    }
}
