package org.figuramc.figura.mixin.gui;

import net.minecraft.client.GuiMessage;
import org.figuramc.figura.ducks.GuiMessageAccessor;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.class)
public class GuiMessageMixin implements GuiMessageAccessor {

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
