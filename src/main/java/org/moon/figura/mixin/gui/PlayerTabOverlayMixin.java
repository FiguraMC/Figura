package org.moon.figura.mixin.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.trust.Trust;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @Unique private UUID uuid;

    @Inject(at = @At("RETURN"), method = "getNameForDisplay", cancellable = true)
    private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        //get config
        int config = Config.LIST_NAMEPLATE.asInt();
        if (config == 0 || AvatarManager.panic)
            return;

        //apply customization
        Component text = cir.getReturnValue();
        Component replacement;
        boolean replaceBadges = false;

        UUID uuid = playerInfo.getProfile().getId();
        Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
        NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.LIST;
        if (custom != null && custom.getText() != null && avatar.trust.get(Trust.NAMEPLATE_EDIT) == 1) {
            replacement = NameplateCustomization.applyCustomization(custom.getText().replaceAll("\n|\\\\n", " "));
            replaceBadges = replacement.getString().contains("${badges}");
        } else {
            replacement = Component.literal(playerInfo.getProfile().getName());
        }

        //badges
        Component badges = config > 1 ? Badges.fetchBadges(uuid) : Component.empty();
        if (replaceBadges) {
            replacement = TextUtils.replaceInText(replacement, "\\$\\{badges\\}", badges);
        } else if (badges.getString().length() > 0) {
            ((MutableComponent) replacement).append(" ").append(badges);
        }

        text = TextUtils.replaceInText(text, "\\b" + Pattern.quote(playerInfo.getProfile().getName()) + "\\b", replacement);

        cir.setReturnValue(text);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPlayerByUUID(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/Player;", shift = At.Shift.BEFORE), method = "render", locals = LocalCapture.CAPTURE_FAILSOFT)
    private void render(PoseStack matrices, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci, ClientPacketListener clientPacketListener, List<PlayerInfo> list, int i, int j, int l, int m, int k, boolean bl, int n, int o, int p, int q, int r, List<FormattedCharSequence> list2, int t, int u, int s, int v, int w, int x, PlayerInfo playerInfo2, GameProfile gameProfile) {
        uuid = gameProfile.getId();
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerFaceRenderer;draw(Lcom/mojang/blaze3d/vertex/PoseStack;IIIZZ)V"))
    private void doNotDrawFace(Args args) {
        if (uuid != null) {
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            if (avatar != null && avatar.renderPortrait(args.get(0), args.get(1), args.get(2), args.get(3), 16, false))
                args.set(3, 0);
        }
    }
}
