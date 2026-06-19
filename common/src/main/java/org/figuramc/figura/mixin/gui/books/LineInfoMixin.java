package org.figuramc.figura.mixin.gui.books;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiLineEditBox.class)
public class LineInfoMixin {

    @WrapOperation(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V"))
    public void test(GuiGraphicsExtractor instance, Font font, String string, int i, int j, int k, boolean bl, Operation<Void> original) {
        Component literal = Component.literal(string);
        Component emojied = Emojis.applyEmojis(literal);
        if (literal != emojied) {
            instance.text(font, emojied, i, j, k, bl); // make it use a component if emojis are present
        } else {
            original.call(instance, font, string, i, j, k, bl);
        }
    }

}
