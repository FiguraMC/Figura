package org.figuramc.figura.gui.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraGui;

public class GuiOverlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float tickDelta, int screenWidth, int screenHeight) {
        if (!AvatarManager.panic)
            FiguraGui.renderOverlays(guiGraphics);
    }
}
