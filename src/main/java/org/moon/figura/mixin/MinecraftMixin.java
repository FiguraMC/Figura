package org.moon.figura.mixin;

import net.minecraft.client.Minecraft;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(at = @At("RETURN"), method = "handleKeybinds")
    public void handleKeybinds(CallbackInfo ci) {
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
}
