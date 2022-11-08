package org.moon.figura.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.Badges;
import org.moon.figura.config.Config;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.trust.Trust;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V")
    private void addMessageEvent(Component message, int messageId, int timestamp, boolean refresh, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null)
            avatar.chatReceivedMessageEvent(message.getString());
    }

    @ModifyVariable(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;IIZ)V", argsOnly = true)
    private Component addMessageName(Component message) {
        //get config
        int config = Config.CHAT_NAMEPLATE.asInt();
        if (config == 0 || this.minecraft.player == null)
            return message;

        //iterate over ALL online players
        for (UUID uuid : this.minecraft.player.connection.getOnlinePlayerIds()) {
            //get player
            PlayerInfo player = this.minecraft.player.connection.getPlayerInfo(uuid);
            if (player == null)
                continue;

            //apply customization
            Component replacement;
            boolean replaceBadges = false;

            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.CHAT;
            if (custom != null && custom.getText() != null && avatar.trust.get(Trust.NAMEPLATE_EDIT) == 1) {
                replacement = NameplateCustomization.applyCustomization(custom.getText().replaceAll("\n|\\\\n", " "));
                if (custom.getText().contains("${badges}"))
                    replaceBadges = true;
            } else {
                replacement = new TextComponent(player.getProfile().getName());
            }

            //badges
            Component badges = config > 1 ? Badges.fetchBadges(uuid) : TextComponent.EMPTY.copy();
            if (replaceBadges) {
                replacement = TextUtils.replaceInText(replacement, "\\$\\{badges\\}", badges);
            } else if (badges.getString().length() > 0) {
                ((MutableComponent) replacement).append(" ").append(badges);
            }

            //modify message
            message = TextUtils.replaceInText(message, "\\b" + player.getProfile().getName() + "\\b", replacement);
        }

        return message;
    }
}
