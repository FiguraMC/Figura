package org.moon.figura.mixin.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.avatars.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.ui.UIHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.UUID;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique private UUID uuid;

    @Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
    private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        //get config
        int config = Config.LIST_NAMEPLATE.asInt();
        if (config == 0)
            return;

        //get data
        UUID uuid = playerInfo.getProfile().getId();
        Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
        if (avatar == null)
            return;

        //apply customization
        Component text = cir.getReturnValue();
        Component replacement;

        NameplateCustomization custom = avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;
        if (custom != null && custom.getText() != null && avatar.trust.get(TrustContainer.Trust.NAMEPLATE_EDIT) == 1) {
            replacement = NameplateCustomization.applyCustomization(custom.getText());
        } else {
            replacement = Component.literal(playerInfo.getProfile().getName());
        }

        if (config > 1) {
            Component badges = Badges.fetchBadges(avatar);
            ((MutableComponent) replacement).append(badges);
        }

        text = TextUtils.replaceInText(text, "\\b" + playerInfo.getProfile().getName() + "\\b", replacement);

        cir.setReturnValue(text);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPlayerByUUID(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/Player;", shift = At.Shift.BEFORE), method = "render", locals = LocalCapture.CAPTURE_FAILHARD)
    private void render(PoseStack matrices, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci, ClientPacketListener clientPacketListener, List<PlayerInfo> list, int i, int j, int l, int m, int k, boolean bl, int n, int o, int p, int q, int r, List<FormattedCharSequence> list2, int t, int u, int s, int v, int w, int x, PlayerInfo playerInfo2, GameProfile gameProfile) {
        uuid = gameProfile.getId();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lcom/mojang/blaze3d/vertex/PoseStack;IIIZZ)V"), method = "render")
    private void drawPlayerFace(PoseStack matrices, int i, int j, int k, boolean bl, boolean bl2) {
        if (uuid != null) {
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            if (avatar != null && avatar.renderHeadOnHud(matrices, i, j, k, 16, false))
                return;
        }

        PlayerFaceRenderer.draw(matrices, i, j, k, bl, bl2);
    }
}
