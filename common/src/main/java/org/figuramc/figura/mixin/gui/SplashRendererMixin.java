package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
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
    private static GuiGraphics gui;

    @Inject(at = @At("HEAD"), method = "render")
    private void render(GuiGraphics graphics, int i, Font textRenderer, int j, CallbackInfo ci) {
        gui = graphics;
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
    private String getSplashWidth(String text) {
        return FiguraMod.splashText == null ? text : FiguraMod.splashText.getString();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"))
    private String drawSplashText(Font font, String text, int centerX, int y, int color) {
        if (FiguraMod.splashText == null || gui == null)
            return text;

        gui.drawCenteredString(font, FiguraMod.splashText, centerX, y, color);
        return "";
    }
}
