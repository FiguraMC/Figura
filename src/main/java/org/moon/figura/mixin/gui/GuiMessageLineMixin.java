package org.moon.figura.mixin.gui;

import net.minecraft.client.GuiMessage;
import org.moon.figura.ducks.GuiMessageAccessor;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public class GuiMessageLineMixin implements GuiMessageAccessor {

    @Unique private int color = 0;

    @Override @Intrinsic
    public void figura$setColor(int color) {
        this.color = color;
    }

    @Override @Intrinsic
    public int figura$getColor() {
        return color;
    }
}
