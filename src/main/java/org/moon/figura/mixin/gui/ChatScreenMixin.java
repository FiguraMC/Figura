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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected EditBox input;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;sendMessage(Ljava/lang/String;)V"), method = "keyPressed")
    private String sendMessage(String text) {
        FiguraRunCommand.canRun = true;

        String s = text;
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && !text.isBlank())
            s = avatar.chatSendMessageEvent(text);

        if (!text.equals(s))
            FiguraMod.LOGGER.info("Changed chat message from \"{}\" to \"{}\"", text, s);

        return s;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;sendMessage(Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "keyPressed")
    private void afterSendMessage(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
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
