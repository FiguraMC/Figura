package org.moon.figura.mixin.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.moon.figura.ducks.SuggestionsListAccessor;
import org.moon.figura.gui.Emojis;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CommandSuggestions.SuggestionsList.class)
public class SuggestionsListMixin implements SuggestionsListAccessor {

    @Shadow @Final private Rect2i rect;

    @Unique private boolean figuraList;

    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I"))
    private void voidTextDraw(Args args) {
        if (!figuraList)
            return;

        PoseStack stack = args.get(0);
        String text = args.get(1);
        float x = args.get(2);
        float y = args.get(3);
        int color = args.get(4);
        Font font = Minecraft.getInstance().font;

        //get emoji
        Component emoji = Emojis.applyEmojis(Component.literal(text));

        //dont render if no emoji was applied
        if (emoji.getString().equals(text))
            return;

        //change text x
        args.set(2, x + 8 + font.width(" "));

        //render emoji
        font.drawShadow(stack, emoji, x + 4 - (int) (font.width(emoji) / 2f), y, color);
    }

    @Override
    @Intrinsic
    public void figura$setFiguraList(boolean bool) {
        figuraList = bool;
        if (bool) this.rect.setWidth(this.rect.getWidth() + 8 + Minecraft.getInstance().font.width(" "));
    }
}
