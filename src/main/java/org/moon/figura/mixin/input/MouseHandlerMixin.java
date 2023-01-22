package org.moon.figura.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Shadow private boolean mouseGrabbed;

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onPress(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (window != this.minecraft.getWindow().getWindow())
            return;

        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.luaRuntime == null)
            return;

        if (avatar.mousePressEvent(button, action, modifiers) && this.mouseGrabbed) {
            ci.cancel();
            return;
        }

        boolean pressed = action != 0;

        if (avatar.luaRuntime != null && FiguraKeybind.set(avatar.luaRuntime.keybinds.keyBindings, InputConstants.Type.MOUSE.getOrCreate(button), pressed))
            ci.cancel();

        if (avatar.luaRuntime != null && pressed && avatar.luaRuntime.host.unlockCursor && this.minecraft.screen == null)
            ci.cancel();

        if (avatar.luaRuntime != null && pressed && ActionWheel.isEnabled()) {
            if (button <= 1) ActionWheel.execute(ActionWheel.getSelected(), button == 0);
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.mouseScrollEvent(scrollDeltaY) && this.mouseGrabbed)
            ci.cancel();

        if (ActionWheel.isEnabled()) {
            ActionWheel.scroll(scrollDeltaY);
            ci.cancel();
        } else if (PopupMenu.isEnabled() && PopupMenu.hasEntity()) {
            PopupMenu.scroll(Math.signum(scrollDeltaY));
            ci.cancel();
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"), cancellable = true)
    private void onMove(long window, double x, double y, CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (avatar != null && avatar.mouseMoveEvent(x - this.xpos, y - this.ypos) && mouseGrabbed) {
            this.xpos = x;
            this.ypos = y;
            ci.cancel();
        }
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void grabMouse(CallbackInfo ci) {
        Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
        if (ActionWheel.isEnabled() || (avatar != null && avatar.luaRuntime != null && avatar.luaRuntime.host.unlockCursor))
            ci.cancel();
    }
}
