package org.moon.figura.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.actionwheel.ActionWheel;
import org.moon.figura.lua.api.SoundAPI;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow @Final public MouseHandler mouseHandler;

    @Unique
    private boolean scriptMouseUnlock = false;

    @Inject(at = @At("RETURN"), method = "handleKeybinds")
    private void handleKeybinds(CallbackInfo ci) {
        if (Config.PANIC_BUTTON.keyBind.consumeClick()) {
            AvatarManager.panic = !AvatarManager.panic;
            FiguraToast.sendToast(FiguraText.of(AvatarManager.panic ? "toast.panic_enabled" : "toast.panic_disabled"), FiguraToast.ToastType.WARNING);
            SoundAPI.getSoundEngine().figura$stopAllSounds();
            return;
        }

        //dont handle keybinds on panic
        if (AvatarManager.panic)
            return;

        if (Config.RELOAD_BUTTON.keyBind.consumeClick())
            AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());

        if (Config.ACTION_WHEEL_BUTTON.keyBind.isDown()) {
            ActionWheel.setEnabled(true);
            this.mouseHandler.releaseMouse();
        } else if (ActionWheel.isEnabled()) {
            ActionWheel.setEnabled(false);
            this.mouseHandler.grabMouse();
        }

        //unlock cursor :p
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.luaRuntime != null && avatar.luaRuntime.host.unlockCursor) {
            this.mouseHandler.releaseMouse();
            scriptMouseUnlock = true;
        } else if (scriptMouseUnlock) {
            this.mouseHandler.grabMouse();
            scriptMouseUnlock = false;
        }
    }

    @Inject(at = @At("HEAD"), method = "setScreen")
    private void setScreen(Screen screen, CallbackInfo ci) {
        if (ActionWheel.isEnabled())
            ActionWheel.setEnabled(false);

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.luaRuntime != null)
            FiguraKeybind.releaseAll(avatar.luaRuntime.keybind.keyBindings);
    }

    @Inject(at = @At("RETURN"), method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V")
    private void clearLevel(Screen screen, CallbackInfo ci) {
        AvatarManager.clearAllAvatars();
    }

    @Inject(at = @At("RETURN"), method = "setLevel")
    private void setLevel(ClientLevel world, CallbackInfo ci) {
        NetworkManager.assertBackend();
    }

    @Inject(at = @At("HEAD"), method = "runTick")
    public void preTick(boolean tick, CallbackInfo ci) {
        AvatarManager.applyAnimations();
    }

    @Inject(at = @At("RETURN"), method = "runTick")
    public void afterTick(boolean tick, CallbackInfo ci) {
        AvatarManager.clearAnimations();
    }
}
