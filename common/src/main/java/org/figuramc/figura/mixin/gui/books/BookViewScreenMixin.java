package org.figuramc.figura.mixin.gui.books;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BookViewScreen.class, priority = 1100)
public class BookViewScreenMixin {
    @ModifyArg(method = "visitText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(IILnet/minecraft/util/FormattedCharSequence;)V"))
    public FormattedCharSequence render(FormattedCharSequence formattedCharSequence) {
        return Emojis.applyEmojis(TextUtils.charSequenceToText(formattedCharSequence)).getVisualOrderText();
    }

    @ModifyArg(method = "visitText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/network/chat/Component;)V"), index = 3)
    public Component render(Component component) {
        return Emojis.applyEmojis(component);
    }
}
