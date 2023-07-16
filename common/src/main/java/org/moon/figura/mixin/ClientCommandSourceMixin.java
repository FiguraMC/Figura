package org.moon.figura.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.moon.figura.utils.FiguraClientCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientSuggestionProvider.class)
abstract class ClientCommandSourceMixin implements FiguraClientCommandSource {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Override
    public void sendFeedback(Component message) {
        this.minecraft.gui.getChat().addMessage(message);
        this.minecraft.getNarrator().sayNow(message);
    }

    @Override
    public void sendError(Component message) {
        sendFeedback(Component.literal("").append(message).withStyle(ChatFormatting.RED));
    }

    @Override
    public Minecraft getClient() {
        return minecraft;
    }

    @Override
    public LocalPlayer getPlayer() {
        return minecraft.player;
    }

    @Override
    public ClientLevel getWorld() {
        return minecraft.level;
    }
}