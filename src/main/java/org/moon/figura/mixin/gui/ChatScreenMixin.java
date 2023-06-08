package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.commands.FiguraRunCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected EditBox input;

    @ModifyVariable(at = @At("HEAD"), method = "handleChatInput", argsOnly = true)
    private String handleChatInput(String text) {
        FiguraRunCommand.canRun = true;

        String s = text;
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && !text.isBlank())
            s = avatar.chatSendMessageEvent(text);

        if (!text.equals(s))
            FiguraMod.LOGGER.info("Changed chat message from \"{}\" to \"{}\"", text, s);

        return s;
    }

    @Inject(at = @At("RETURN"), method = "handleChatInput")
    private void afterHandleChatInput(String text, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        FiguraRunCommand.canRun = false;
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void render(PoseStack poseStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaRuntime == null)
            return;

        Integer color = avatar.luaRuntime.host.chatColor;
        if (color == null)
            return;

        this.input.setTextColor(color);
    }
}
