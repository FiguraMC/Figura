package org.figuramc.figura.mixin.gui.books;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BookEditScreen.LineInfo.class)
public class LineInfoMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/MutableComponent;setStyle(Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/MutableComponent;"))
    public MutableComponent test(MutableComponent instance, Style style) {
        return Emojis.applyEmojis(instance.setStyle(style));
    }

}
