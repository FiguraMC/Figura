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

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaState == null)
            return;

        boolean pressed = action != 0;
        FiguraKeybind.set(avatar.luaState.keybind.keyBindings, InputConstants.Type.MOUSE.getOrCreate(button), pressed);

        if (pressed) {
            if (ActionWheel.isEnabled())
                ActionWheel.execute(button == 0);

            if (avatar.luaState.host.unlockCursor)
                ci.cancel();
        }
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void grabMouse(CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.luaState != null && avatar.luaState.host.unlockCursor)
            ci.cancel();
    }
}
