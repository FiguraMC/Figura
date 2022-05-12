package org.moon.figura.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(at = @At("RETURN"), method = "handleKeybinds")
    private void handleKeybinds(CallbackInfo ci) {
        if (Config.PANIC_BUTTON.keyBind.consumeClick()) {
            AvatarManager.panic = !AvatarManager.panic;
            FiguraToast.sendToast(new FiguraText(AvatarManager.panic ? "toast.panic_enabled" : "toast.panic_disabled"), FiguraToast.ToastType.WARNING);
            return;
        }

        //dont handle keybinds on panic
        if (AvatarManager.panic)
            return;

        if (Config.RELOAD_BUTTON.keyBind.consumeClick())
            AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
    }

    @Inject(at = @At("HEAD"), method = "setScreen")
    private void setScreen(Screen screen, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.luaState != null)
            FiguraKeybind.releaseAll(avatar.luaState.keybind.keyBindings);
    }
}
