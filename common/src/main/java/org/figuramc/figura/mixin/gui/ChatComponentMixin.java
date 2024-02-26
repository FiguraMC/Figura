package org.figuramc.figura.mixin.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.avatar.Badges;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.lua.api.nameplate.NameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.EntityUtils;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Unique private Integer color;
    @Unique private int currColor;

    @ModifyVariable(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", ordinal = 0, argsOnly = true)
    private Component addMessage(Component message, Component msg, MessageSignature signature, int k, GuiMessageTag tag, boolean refresh) {
        // do not change the message on refresh
        if (refresh) return message;

        color = null;
        if (AvatarManager.panic)
            return message;

        // receive event
        Avatar localPlayer = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (localPlayer != null) {
            String json = Component.Serializer.toJson(message);

            Pair<String, Integer> event = localPlayer.chatReceivedMessageEvent(message.getString(), json);
            if (event != null) {
                String newMessage = event.getFirst();
                if (newMessage == null)
                    return null;
                if (!json.equals(newMessage)) {
                    TextUtils.allowScriptEvents = true;
                    message = TextUtils.tryParseJson(newMessage);
                    TextUtils.allowScriptEvents = false;
                }
                color = event.getSecond();
            }
        }

        // stop here if we should not parse messages
        if (!FiguraMod.parseMessages)
            return message;

        // emojis
        if (Configs.EMOJIS.value > 0)
            message = Emojis.applyEmojis(message);

        // nameplates
        int config = Configs.CHAT_NAMEPLATE.value;
        if (config == 0)
            return message;

        message = TextUtils.parseLegacyFormatting(message);

        Map<String, UUID> players = EntityUtils.getPlayerList();
        String owner = null;

        String msgString = message.getString();
        String[] split = msgString.split("\\W+");
        for (String s : split) {
            if (players.containsKey(s)) {
                owner = s;
                break;
            }
        }

        // iterate over ALL online players
        for (Map.Entry<String, UUID> entry : players.entrySet()) {
            String name = entry.getKey();

            if (!msgString.toLowerCase(Locale.US).contains(name.toLowerCase(Locale.US))) // player is not here
                continue;

            UUID uuid = entry.getValue();
            boolean isOwner = name.equals(owner);

            Component playerName = Component.literal(name);

            // apply customization
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);
            NameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.CHAT;

            if (custom == null && config < 2) // no customization and no possible badges to append
                continue;

            Component replacement = custom != null && custom.getJson() != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1 ?
                    TextUtils.replaceInText(custom.getJson().copy(), "\n|\\\\n", " ") : playerName;

            // name
            replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", playerName);

            // badges
            Component emptyReplacement = Badges.appendBadges(replacement, uuid, config > 1 && owner == null);

            // trim
            emptyReplacement = TextUtils.trim(emptyReplacement);

            // modify message
            String quotedName = "(?i)\\b" + Pattern.quote(name) + "\\b";
            message = TextUtils.replaceInText(message, quotedName, emptyReplacement, (s, style) -> true, isOwner ? 1 : 0, Integer.MAX_VALUE);

            // sender badges
            if (config > 1 && isOwner) {
                // badges
                Component temp = Badges.appendBadges(replacement, uuid, true);
                // trim
                temp = TextUtils.trim(temp);
                // modify message, only first
                message = TextUtils.replaceInText(message, quotedName, temp, (s, style) -> true, 1);
            }
        }

        return message;
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", cancellable = true)
    private void addMessage(Component message, MessageSignature signature, int ticks, GuiMessageTag tag, boolean refresh, CallbackInfo ci) {
        if (message == null)
            ci.cancel();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V")
    private Object addMessages(int index, Object message) {
        if (color != null) ((GuiMessageAccessor) message).figura$setColor(color);
        return message;
    }

    @ModifyVariable(at = @At("STORE"), method = "render")
    private GuiMessage.Line grabColor(GuiMessage.Line line) {
        currColor = ((GuiMessageAccessor) (Object) line).figura$getColor();
        return line;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0), method = "render", index = 4)
    private int textBackgroundOnRender(int color) {
        return color + currColor;
    }

    @ModifyVariable(at = @At("STORE"), method = "refreshTrimmedMessage")
    private GuiMessage refreshMessages(GuiMessage message) {
        color = ((GuiMessageAccessor) (Object) message).figura$getColor();
        return message;
    }
}
