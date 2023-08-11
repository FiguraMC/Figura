package org.figuramc.figura.mixin.gui.books;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.util.FormattedCharSequence;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BookViewScreen.class)
public class BookViewScreenMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I"))
    public int render(Font font, PoseStack poseStack, FormattedCharSequence formattedCharSequence, float x, float y, int color) {
        return font.draw(poseStack, Emojis.applyEmojis(TextUtils.charSequenceToText(formattedCharSequence)), x, y, color);
    }
}
