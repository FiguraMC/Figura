package org.figuramc.figura.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLKeyboard;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.keybind.FiguraKeybind;
import org.figuramc.figura.utils.IOUtils;
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
    private void keyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().handle())
            return;

        if (action == 1 && Configs.PANIC_BUTTON.keyBind.matches(event)) {
            AvatarManager.togglePanic();
            ci.cancel();
        }

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaRuntime == null)
            return;

        if (avatar.keyPressEvent(IOUtils.toLegacyInputCode(InputConstants.getKey(event)), action, IOUtils.toLegacyModifiers(event.modifiers())) && (this.minecraft.mouseHandler.isMouseGrabbed() || this.minecraft.gui.screen() == null)) {
            ci.cancel();
            return;
        }

        if (avatar.luaRuntime != null && FiguraKeybind.set(avatar.luaRuntime.keybinds.keyBindings, InputConstants.getKey(event), action != 0, event.modifiers())) {
            KeyMapping.setAll();
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"))
    private void charTyped(long window, CharacterEvent characterEvent, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().handle())
            return;

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null)
            avatar.charTypedEvent(Character.toString(characterEvent.codepoint()), IOUtils.toLegacyModifiers(SDLKeyboard.SDL_GetModState()), characterEvent.codepoint());
    }
}
