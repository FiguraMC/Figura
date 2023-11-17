package org.figuramc.figura.gui.neoforge;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraGui;

public class GuiOverlay implements IGuiOverlay {
    @Override
    public void render(ExtendedGui forgeGui, GuiGraphics guiGraphics, float tickDelta, int screenWidth, int screenHeight) {
        if (!AvatarManager.panic)
            FiguraGui.renderOverlays(guiGraphics);
    }
}
