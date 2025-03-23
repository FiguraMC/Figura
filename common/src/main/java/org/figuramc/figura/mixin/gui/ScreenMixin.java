package org.figuramc.figura.mixin.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraFunctionClickEvent;
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
            figuraEvent.onClick().run();
            cir.setReturnValue(true);
        } else if (event instanceof FiguraFunctionClickEvent) {
            cir.setReturnValue(true);

            Avatar avatar = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());
            if (avatar == null)
                return;
            LuaValue value = null;
            try {
                value = avatar.loadScript("figura_function", ((FiguraFunctionClickEvent) event).chunk());
            } catch (Exception e) {
                FiguraMod.sendChatMessage(Component.literal(e.getMessage()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)));
            }
            if (value != null)
                avatar.run(value, avatar.tick);
        }
    }
}
