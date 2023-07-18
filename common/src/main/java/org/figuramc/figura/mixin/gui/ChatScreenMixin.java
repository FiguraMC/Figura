package org.figuramc.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
<<<<<<< HEAD:src/main/java/org/moon/figura/mixin/gui/ChatScreenMixin.java
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.commands.FiguraRunCommand;
=======
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/mixin/gui/ChatScreenMixin.java
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
<<<<<<< HEAD:src/main/java/org/moon/figura/mixin/gui/ChatScreenMixin.java
=======
import org.spongepowered.asm.mixin.injection.ModifyVariable;
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/mixin/gui/ChatScreenMixin.java
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow protected EditBox input;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;sendMessage(Ljava/lang/String;)V"), method = "keyPressed")
    private String sendMessage(String text) {
<<<<<<< HEAD:src/main/java/org/moon/figura/mixin/gui/ChatScreenMixin.java
        FiguraRunCommand.canRun = true;

=======
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/mixin/gui/ChatScreenMixin.java
        String s = text;
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && !text.isBlank())
            s = avatar.chatSendMessageEvent(text);

        if (!text.equals(s))
            FiguraMod.LOGGER.info("Changed chat message from \"{}\" to \"{}\"", text, s);

        return s;
<<<<<<< HEAD:src/main/java/org/moon/figura/mixin/gui/ChatScreenMixin.java
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;sendMessage(Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "keyPressed")
    private void afterSendMessage(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        FiguraRunCommand.canRun = false;
=======
>>>>>>> 8b2c2606120aac4f05d8dd5820ea17317422dc93:common/src/main/java/org/figuramc/figura/mixin/gui/ChatScreenMixin.java
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaRuntime == null)
            return;

        Integer color = avatar.luaRuntime.host.chatColor;
        if (color == null)
            return;

        this.input.setTextColor(color);
    }
}
