package org.moon.figura.mixin.render.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.moon.figura.lua.api.vanilla_model.VanillaPart;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.permissions.Permissions;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void renderFiguraLabelIfPresent(AbstractClientPlayer player, Component text, PoseStack stack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        //return on config or high entity distance
        int config = Config.ENTITY_NAMEPLATE.asInt();
        if (config == 0 || AvatarManager.panic || this.entityRenderDispatcher.distanceToSqr(player) > 4096)
            return;

        //get customizations
        Avatar avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        EntityNameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.ENTITY;

        //enabled
        if (custom != null && !custom.visible) {
            ci.cancel();
            return;
        }

        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler(player.getName().getString());
        FiguraMod.pushProfiler("nameplate");

        //trust check
        boolean trust = avatar != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;

        stack.pushPose();

        //pivot
        FiguraMod.pushProfiler("pivot");
        FiguraVec3 pivot;
        if (custom != null && custom.getPivot() != null && trust)
            pivot = custom.getPivot();
        else
            pivot = FiguraVec3.of(0f, player.getBbHeight() + 0.5f, 0f);

        stack.translate(pivot.x, pivot.y, pivot.z);

        //rotation
        stack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        //pos
        FiguraMod.popPushProfiler("position");
        if (custom != null && custom.getPos() != null && trust) {
            FiguraVec3 pos = custom.getPos();
            stack.translate(pos.x, pos.y, pos.z);
        }

        //scale
        FiguraMod.popPushProfiler("scale");
        float scale = 0.025f;
        FiguraVec3 scaleVec = FiguraVec3.of(-scale, -scale, -scale);
        if (custom != null && custom.getScale() != null && trust)
            scaleVec.multiply(custom.getScale());

        stack.scale((float) scaleVec.x, (float) scaleVec.y, (float) scaleVec.z);

        //text
        Component name = new TextComponent(player.getName().getString());
        FiguraMod.popPushProfiler("text");
        Component replacement = custom != null && custom.getJson() != null && trust ? custom.getJson().copy() : name;

        //name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        //badges
        FiguraMod.popPushProfiler("badges");
        replacement = Badges.appendBadges(replacement, player.getUUID(), config > 1);

        FiguraMod.popPushProfiler("applyName");
        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(player.getName().getString()) + "\\b", replacement);

        // * variables * //
        FiguraMod.popPushProfiler("colors");
        boolean isSneaking = player.isDiscrete();
        boolean deadmau = text.getString().equals("deadmau5");

        boolean hasCustom = trust && custom != null;

        double bgOpacity = hasCustom && custom.alpha != null ? custom.alpha : Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        int bgColor = (hasCustom && custom.background != null ? custom.background : 0) + ((int) (bgOpacity * 0xFF) << 24);
        int outlineColor = hasCustom && custom.outlineColor != null ? custom.outlineColor : 0x202020;

        boolean outline = hasCustom && custom.outline;
        boolean shadow = hasCustom && custom.shadow;

        light = hasCustom && custom.light != null ? custom.light : light;

        Matrix4f matrix4f = stack.last().pose();
        Font font = this.getFont();

        //render scoreboard
        FiguraMod.popPushProfiler("render");
        FiguraMod.pushProfiler("scoreboard");
        boolean hasScore = false;
        if (this.entityRenderDispatcher.distanceToSqr(player) < 100) {
            //get scoreboard
            Scoreboard scoreboard = player.getScoreboard();
            Objective scoreboardObjective = scoreboard.getDisplayObjective(2);
            if (scoreboardObjective != null) {
                hasScore = true;

                //render scoreboard
                Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), scoreboardObjective);

                Component text1 = new TextComponent(Integer.toString(score.getScore())).append(" ").append(scoreboardObjective.getDisplayName());
                float x = -font.width(text1) / 2f;
                float y = deadmau ? -10f : 0f;

                font.drawInBatch(text1, x, y, 0x20FFFFFF, false, matrix4f, multiBufferSource, !isSneaking, bgColor, light);
                if (!isSneaking) {
                    if (outline)
                        font.drawInBatch8xOutline(text1.getVisualOrderText(), x, y, -1, outlineColor, matrix4f, multiBufferSource, light);
                    else
                        font.drawInBatch(text1, x, y, -1, shadow, matrix4f, multiBufferSource, false, 0, light);
                }
            }
        }

        //render name
        FiguraMod.popPushProfiler("name");
        List<Component> textList = TextUtils.splitText(text, "\n");

        for (int i = 0; i < textList.size(); i++) {
            Component text1 = textList.get(i);

            if (text1.getString().isEmpty())
                continue;

            int line = i - textList.size() + (hasScore ? 0 : 1);

            float x = -font.width(text1) / 2f;
            float y = (deadmau ? -10f : 0f) + (font.lineHeight + 1.5f) * line;

            font.drawInBatch(text1, x, y, 0x20FFFFFF, false, matrix4f, multiBufferSource, !isSneaking, bgColor, light);
            if (!isSneaking) {
                if (outline)
                    font.drawInBatch8xOutline(text1.getVisualOrderText(), x, y, -1, outlineColor, matrix4f, multiBufferSource, light);
                else
                    font.drawInBatch(text1, x, y, -1, shadow, matrix4f, multiBufferSource, false, 0, light);
            }
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
        int overlay = getOverlayCoords(player, getWhiteOverlayProgress(player, delta));
        avatar.firstPersonRender(stack, multiBufferSource, player, (PlayerRenderer) (Object) this, arm, light, overlay, delta);

        if (avatar.luaRuntime != null)
            avatar.luaRuntime.vanilla_model.PLAYER.restore(this.getModel());
    }
}
