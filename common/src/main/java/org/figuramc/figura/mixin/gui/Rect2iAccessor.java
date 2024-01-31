package org.figuramc.figura.mixin.gui;

import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Rect2i.class)
public interface Rect2iAccessor {
    @Intrinsic
    @Accessor("width")
    void setWidth(int x);

    @Intrinsic
    @Accessor("height")
    void setHeight(int y);
}
