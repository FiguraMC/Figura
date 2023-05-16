package org.moon.figura.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.moon.figura.ducks.SuggestionsListAccessor;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin implements SuggestionsListAccessor {

    @Shadow @Final private Rect2i rect;

    @Unique private boolean figuraList;
    @Unique private static GuiGraphics gui;

    @Inject(at = @At("HEAD"), method = "render")
    private void onRender(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        gui = graphics;
    }

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"))
    private void voidTextDraw(Args args) {
        if (!figuraList || gui == null)
            return;

        Font font = args.get(0);
        String text = args.get(1);
        int x = args.get(2);
        int y = args.get(3);
        int color = args.get(4);

        //get emoji
        Component emoji = Emojis.applyEmojis(Component.literal(text));

        //dont render if no emoji was applied
        if (emoji.getString().equals(text))
            return;

        //change text x
        args.set(2, x + 8 + font.width(" "));

        //render emoji
        gui.drawString(font, emoji, x + 4 - font.width(emoji) / 2, y, color);
    }

    @Override
    @Intrinsic
    public void figura$setFiguraList(boolean bool) {
        figuraList = bool;
        if (bool) this.rect.setWidth(this.rect.getWidth() + 8 + Minecraft.getInstance().font.width(" "));
    }
}
