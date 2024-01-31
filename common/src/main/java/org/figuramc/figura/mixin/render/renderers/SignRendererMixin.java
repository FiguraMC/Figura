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

    // method_32159 corresponds to fabric intermediary, lambda$renderSignText$2 is the unmapped OF name, m_244733_ is the SRG name for Forge
    //TODO: Set last remap to false, add the rest, OF is lambda$render$0
    @ModifyArg(method = "method_3583", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;", remap = true), remap = true)
    private static FormattedText modifyText(FormattedText charSequence) {
        return Configs.EMOJIS.value > 0 && charSequence instanceof Component text ? Emojis.applyEmojis(text) : charSequence;
    }
}
