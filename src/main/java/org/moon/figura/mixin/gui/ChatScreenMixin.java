package org.moon.figura.mixin.gui;

import net.minecraft.client.gui.screens.ChatScreen;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/ChatScreen;sendMessage(Ljava/lang/String;)V"), method = "keyPressed")
    private void keyPressed(ChatScreen instance, String s) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null) {
            String str = avatar.chatSendMessageEvent(s);

            if (str == null)
                return;

            s = str;
        }

        instance.sendMessage(s);
    }
}
