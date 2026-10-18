package org.figuramc.figura.gui.neoforge;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraGui;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiOverlay implements GuiLayer {
    @Override
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!AvatarManager.panic)
            FiguraGui.renderOverlays(guiGraphics);
    }
}
