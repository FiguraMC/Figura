package org.figuramc.figura.gui.neoforge;


import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.figuramc.figura.gui.FiguraGui;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiUnderlay implements GuiLayer {
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        FiguraGui.onRender(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false), new CallbackInfo("dummy", true));
    }
}