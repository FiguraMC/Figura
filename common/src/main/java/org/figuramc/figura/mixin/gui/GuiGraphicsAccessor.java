package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsAccessor {
    @Accessor("guiRenderState")
    GuiRenderState figura$getRenderState();

    @Accessor("scissorStack")
    GuiGraphicsExtractor.ScissorStack figura$getScissorStack();
}
