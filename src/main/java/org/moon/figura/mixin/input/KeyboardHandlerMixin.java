package org.moon.figura.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.config.Configs;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void keyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow())
            return;

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaRuntime == null)
            return;

        if (avatar.keyPressEvent(key, action, modifiers) && (this.minecraft.mouseHandler.isMouseGrabbed() || this.minecraft.screen == null)) {
            ci.cancel();
            return;
        }

        if (avatar.luaRuntime != null && FiguraKeybind.set(avatar.luaRuntime.keybinds.keyBindings, InputConstants.getKey(key, scancode), action != 0, modifiers)) {
            KeyMapping.setAll();
            ci.cancel();
        }
    }

    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;matches(II)Z", ordinal = 0), cancellable = true)
    private void processGlobalKeybinds(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (Configs.PANIC_BUTTON.keyBind.matches(key, scancode)) {
            AvatarManager.togglePanic();
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"))
    private void charTyped(long window, int codePoint, int modifiers, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null)
            avatar.charTypedEvent(Character.toString(codePoint), modifiers, codePoint);
    }
}
