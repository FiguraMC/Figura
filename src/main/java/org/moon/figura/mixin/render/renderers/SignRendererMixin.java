package org.moon.figura.mixin.render.renderers;

import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.Emojis;
import org.moon.figura.utils.TextUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SignRenderer.class)
public class SignRendererMixin {

    @ModifyVariable(method = "renderSignText", at = @At("STORE"))
    private FormattedCharSequence modifyText(FormattedCharSequence charSequence) {
        if (Configs.EMOJIS.value == 0)
            return charSequence;

        Component text = TextUtils.charSequenceToText(charSequence);
        text = Emojis.applyEmojis(text);
        return text.getVisualOrderText();
    }
}
