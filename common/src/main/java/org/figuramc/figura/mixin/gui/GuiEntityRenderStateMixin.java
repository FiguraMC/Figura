package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.figuramc.figura.ducks.GuiEntityRenderStateExtension;
import org.figuramc.figura.model.rendering.EntityRenderMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GuiEntityRenderState.class)
public abstract class GuiEntityRenderStateMixin implements GuiEntityRenderStateExtension {

    @Unique
    double figura$xPos = 0, figura$yPos = 0;

    @Unique
    EntityRenderMode figura$renderMode = EntityRenderMode.MINECRAFT_GUI;

    @Override
    public double getXPos() {
        return figura$xPos;
    }

    @Override
    public void setXPos(double xPos) {
        this.figura$yPos = xPos;
    }

    @Override
    public double getYPos() {
        return figura$yPos;
    }

    @Override
    public void setYPos(double yPos) {
        this.figura$yPos = yPos;
    }

    @Override
    public EntityRenderMode getRenderMode() {
        return figura$renderMode;
    }

    @Override
    public void setRenderMode(EntityRenderMode renderMode) {
        this.figura$renderMode = renderMode;
    }
}
