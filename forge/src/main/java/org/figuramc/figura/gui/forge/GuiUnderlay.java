package org.figuramc.figura.gui.forge;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.figuramc.figura.gui.FiguraGui;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiUnderlay implements IGuiOverlay {
    @Override
    public void render(ForgeGui gui, PoseStack pose, float tickDelta, int screenWidth, int screenHeight) {
        FiguraGui.onRender(pose, tickDelta, new CallbackInfo("dummy", true));
    }
}