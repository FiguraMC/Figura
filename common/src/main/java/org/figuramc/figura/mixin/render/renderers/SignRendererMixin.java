package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.font.Emojis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SignRenderer.class)
public class SignRendererMixin {

    // method_45799 corresponds to fabric intermediary, lambda$renderSignText$2 is the unmapped OF name, m_276705_ is the SRG name for Forge
    @ModifyArg(method = {"method_45799", "lambda$renderSignText$2", "m_276705_"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;", remap = true), remap = false)
    private FormattedText modifyText(FormattedText charSequence) {
        return Configs.EMOJIS.value > 0 && charSequence instanceof Component text ? Emojis.applyEmojis(text) : charSequence;
    }
}
