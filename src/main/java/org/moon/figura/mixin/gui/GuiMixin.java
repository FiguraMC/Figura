package org.moon.figura.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.trust.TrustContainer;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.UUID;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

    @ModifyArgs(method = "handlePlayerChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/chat/ChatListener;handle(Lnet/minecraft/network/chat/ChatType;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatSender;)V"))
    private void handlePlayerChat(Args args) {
        //get config
        int config = (int) Config.CHAT_NAMEPLATE.value;

        if (config == 0 || this.minecraft.player == null)
            return;

        //iterate over ALL online players
        for (UUID uuid : this.minecraft.player.connection.getOnlinePlayerIds()) {
            //get player
            PlayerInfo player = this.minecraft.player.connection.getPlayerInfo(uuid);
            if (player == null)
                continue;

            //get metadata
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            if (avatar == null)
                return;

            //apply customization
            Component replacement;
            Component message = args.get(1);
            NameplateCustomization custom = avatar.luaState == null ? null : avatar.luaState.nameplate.CHAT;
            if (custom != null && NameplateCustomization.getText(custom) != null && TrustManager.get(uuid).get(TrustContainer.Trust.NAMEPLATE_EDIT) == 1) {
                replacement = NameplateCustomization.applyCustomization(NameplateCustomization.getText(custom).replaceAll("\n|\\\\n", ""));
            } else {
                replacement = Component.literal(player.getProfile().getName());
            }

            //apply nameplate
            if (config > 1) {
                Component badges = NameplateCustomization.fetchBadges(avatar);
                ((MutableComponent) replacement).append(badges);
            }

            //modify arg
            args.set(1, TextUtils.replaceInText(message, "\\b" + player.getProfile().getName() + "\\b", replacement));
        }
    }
}
