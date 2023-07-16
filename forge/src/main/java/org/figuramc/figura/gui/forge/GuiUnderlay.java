package org.figuramc.figura.gui.forge;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.figuramc.figura.gui.FiguraGui;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiUnderlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float tickDelta, int screenWidth, int screenHeight) {
        FiguraGui.onRender(guiGraphics, tickDelta, new CallbackInfo("dummy", true));
    }
}