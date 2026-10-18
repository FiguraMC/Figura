package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashRenderer.class)
public class SplashRendererMixin {

    @Unique
    private static GuiGraphicsExtractor gui;

    @Inject(at = @At("HEAD"), method = "extractRenderState")
    private void render(GuiGraphicsExtractor graphics, int i, Font textRenderer, float j, CallbackInfo ci) {
        gui = graphics;
    }

    @ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
    private FormattedText getSplashWidth(FormattedText formattedText) {
        return FiguraMod.splashText == null ? formattedText : FiguraMod.splashText;
    }

    @ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ActiveTextCollector;accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/network/chat/Component;)V"), index = 4)
    private Component drawSplashText(Component component) {
        if (FiguraMod.splashText == null || gui == null)
            return component;
        return FiguraMod.splashText;
    }
}
