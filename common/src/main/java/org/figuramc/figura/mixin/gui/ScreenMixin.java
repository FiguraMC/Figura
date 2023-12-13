package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.utils.TextUtils;
import org.luaj.vm2.LuaValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(at = @At("HEAD"), method = "handleComponentClicked", cancellable = true)
    private void handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style == null)
            return;

        ClickEvent event = style.getClickEvent();
        if (event == null)
            return;

        if (event instanceof TextUtils.FiguraClickEvent figuraEvent) {
            figuraEvent.onClick.run();
            cir.setReturnValue(true);
        } else if (event.getAction() == ClickEvent.Action.valueOf("FIGURA_FUNCTION")) {
            cir.setReturnValue(true);

            Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
            if (avatar == null)
                return;

            LuaValue value = avatar.loadScript("figura_function", event.getValue());
            if (value != null)
                avatar.run(value, avatar.tick);
        }
    }
}
