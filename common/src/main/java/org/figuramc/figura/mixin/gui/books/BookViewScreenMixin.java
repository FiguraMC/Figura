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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BookViewScreen.class, priority = 1100)
public class BookViewScreenMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V"))
    public void render(GuiGraphics graphics, Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color, boolean shadowed) {
        graphics.drawString(font, Emojis.applyEmojis(TextUtils.charSequenceToText(formattedCharSequence)), x, y, color, shadowed);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
    public void render(GuiGraphics graphics, Font font, Component component, int x, int y, int color, boolean shadowed) {
        graphics.drawString(font, Emojis.applyEmojis(component), x, y, color, shadowed);
    }
}
