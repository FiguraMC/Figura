package org.figuramc.figura.gui.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraGui;

public class GuiOverlay implements IIngameOverlay {
    @Override
    public void render(ForgeIngameGui forgeGui, PoseStack pose, float tickDelta, int screenWidth, int screenHeight) {
        if (!AvatarManager.panic)
            FiguraGui.renderOverlays(pose);
    }
}
