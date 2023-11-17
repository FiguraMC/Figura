package org.figuramc.figura.gui.neoforge;


import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import org.figuramc.figura.gui.FiguraGui;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiUnderlay implements IGuiOverlay {
    @Override
    public void render(ExtendedGui gui, GuiGraphics guiGraphics, float tickDelta, int screenWidth, int screenHeight) {
        FiguraGui.onRender(guiGraphics, tickDelta, new CallbackInfo("dummy", true));
    }
}