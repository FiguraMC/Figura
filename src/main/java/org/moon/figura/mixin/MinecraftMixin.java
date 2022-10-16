package org.moon.figura.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.backend.NetworkManager;
import org.moon.figura.config.Config;
import org.moon.figura.gui.FiguraToast;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.lua.api.particle.ParticleAPI;
import org.moon.figura.lua.api.sound.SoundAPI;
import org.moon.figura.utils.EntityUtils;
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
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Final public Options options;

    @Unique
    private boolean scriptMouseUnlock = false;

    @Inject(at = @At("RETURN"), method = "handleKeybinds")
    private void handleKeybinds(CallbackInfo ci) {
        if (Config.PANIC_BUTTON.keyBind.consumeClick()) {
            AvatarManager.panic = !AvatarManager.panic;
            FiguraToast.sendToast(FiguraText.of(AvatarManager.panic ? "toast.panic_enabled" : "toast.panic_disabled"), FiguraToast.ToastType.WARNING);
            SoundAPI.getSoundEngine().figura$stopAllSounds();
            ParticleAPI.getParticleEngine().figura$clearAllParticles();
            return;
        }

        //dont handle keybinds on panic
        if (AvatarManager.panic)
            return;

        if (Config.RELOAD_BUTTON.keyBind.consumeClick()) {
            AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
            FiguraToast.sendToast(FiguraText.of("toast.reload"));
        }

        if (Config.ACTION_WHEEL_BUTTON.keyBind.isDown()) {
            ActionWheel.setEnabled(true);
            this.mouseHandler.releaseMouse();
        } else if (ActionWheel.isEnabled()) {
            ActionWheel.setEnabled(false);
            this.mouseHandler.grabMouse();
        }

        if (Config.POPUP_BUTTON.keyBind.isDown()) {
            PopupMenu.setEnabled(true);

            if (!PopupMenu.hasEntity()) {
                Entity target = EntityUtils.getViewedEntity(32);
                if (this.player != null && target instanceof Player && !target.isInvisibleTo(this.player)) {
                    PopupMenu.setEntity(target);
                } else if (!this.options.getCameraType().isFirstPerson()) {
                    PopupMenu.setEntity(this.player);
                }
            }

            for (int i = this.options.keyHotbarSlots.length - 1; i >= 0; i--) {
                if (this.options.keyHotbarSlots[i].isDown()) {
                    PopupMenu.hotbarKeyPressed(i);
                    break;
                }
            }
        } else if (PopupMenu.isEnabled()) {
            PopupMenu.run();
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

        if (PopupMenu.isEnabled())
            PopupMenu.run();
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
    private void preTick(boolean tick, CallbackInfo ci) {
        AvatarManager.applyAnimations();
    }

    @Inject(at = @At("RETURN"), method = "runTick")
    private void afterTick(boolean tick, CallbackInfo ci) {
        AvatarManager.clearAnimations();
    }
}
