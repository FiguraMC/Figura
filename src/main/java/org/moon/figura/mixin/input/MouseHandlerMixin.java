package org.moon.figura.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.gui.actionwheel.ActionWheel;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onPress(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window != Minecraft.getInstance().getWindow().getWindow())
            return;

        boolean pressed = action != 0;

        if (pressed && (ActionWheel.isEnabled())) {
            if (button <= 1) ActionWheel.execute(button == 0);
            ci.cancel();
        }

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaState == null)
            return;

        if (pressed && avatar.luaState.host.unlockCursor)
            ci.cancel();

        //this needs to be last because it executes functions and can cause lua errors, making luaState null
        FiguraKeybind.set(avatar.luaState.keybind.keyBindings, InputConstants.Type.MOUSE.getOrCreate(button), pressed);
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void grabMouse(CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.luaState != null && avatar.luaState.host.unlockCursor)
            ci.cancel();
    }
}
