package org.moon.figura.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float shadowRadius) {
        super(context, entityModel, shadowRadius);
    }

    @Inject(method = "renderNameTag(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void renderFiguraLabelIfPresent(AbstractClientPlayer player, Component text, PoseStack stack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        //config
        int config = (int) Config.ENTITY_NAMEPLATE.value;

        //get metadata
        Avatar avatar = AvatarManager.getAvatarForPlayer(player.getUUID());
        if (avatar == null || config == 0)
            return;

        //check entity distance
        if (this.entityRenderDispatcher.distanceToSqr(player) > 4096)
            return;

        //get customizations
        NameplateCustomization custom = avatar.luaState == null ? null : avatar.luaState.nameplate.ENTITY;

        //enabled
        if (custom != null && custom.visible != null && !custom.visible) {
            ci.cancel();
            return;
        }

        //trust check
        boolean trust = TrustManager.get(player.getUUID()).get(TrustContainer.Trust.NAMEPLATE_EDIT) == 1;

        stack.pushPose();

        //pos
        FiguraVec3 pos = FiguraVec3.of(0f, player.getBbHeight() + 0.5f, 0f);
        if (custom != null && custom.position != null && trust)
            pos.add(custom.position);

        stack.translate(pos.x, pos.y, pos.z);

        //rotation
        stack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        //scale
        float scale = 0.025f;
        FiguraVec3 scaleVec = FiguraVec3.of(-scale, -scale, scale);
        if (custom != null && custom.scale != null && trust)
            scaleVec.multiply(custom.scale);

        stack.scale((float) scaleVec.x, (float) scaleVec.y, (float) scaleVec.z);

        //text
        Component replacement;
        if (custom != null && custom.text != null && trust) {
            replacement = NameplateCustomization.applyCustomization(custom.text);
        } else {
            replacement = new TextComponent(player.getName().getString());
        }

        if (config > 1) {
            Component badges = NameplateCustomization.fetchBadges(avatar);
            ((MutableComponent) replacement).append(badges);
        }

        text = TextUtils.replaceInText(text, "\\b" + player.getName().getString() + "\\b", replacement);

        // * variables * //
        boolean isSneaking = player.isDiscrete();
        boolean deadmau = "deadmau5".equals(text.getString());

        float bgOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        int bgColor = (int) (bgOpacity * 0xFF) << 24;

        Matrix4f matrix4f = stack.last().pose();
        Font font = this.getFont();

        //render scoreboard
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
                if (!isSneaking)
                    font.drawInBatch(text1, x, y, -1, false, matrix4f, multiBufferSource, false, 0, light);
            }
        }

        //render name
        List<Component> textList = TextUtils.splitText(text, "\n");

        for (int i = 0; i < textList.size(); i++) {
            Component text1 = textList.get(i);
            int line = i - textList.size() + (hasScore ? 0 : 1);

            float x = -font.width(text1) / 2f;
            float y = (deadmau ? -10f : 0f) + (font.lineHeight + 1.5f) * line;

            font.drawInBatch(text1, x, y, 0x20FFFFFF, false, matrix4f, multiBufferSource, !isSneaking, bgColor, light);
            if (!isSneaking)
                font.drawInBatch(text1, x, y, -1, false, matrix4f, multiBufferSource, false, 0, light);
        }

        stack.popPose();
        ci.cancel();
    }
}
