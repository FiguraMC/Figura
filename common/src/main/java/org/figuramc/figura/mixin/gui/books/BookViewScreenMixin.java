package org.figuramc.figura.mixin.gui.books;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.util.FormattedCharSequence;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BookViewScreen.class)
public class BookViewScreenMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I"))
    public int render(GuiGraphics graphics, Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color, boolean shadowed) {

        return graphics.drawString(font, Emojis.applyEmojis(TextUtils.charSequenceToText(formattedCharSequence)), x, y, color, shadowed);
    }
}
