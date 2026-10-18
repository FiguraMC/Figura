package org.figuramc.figura.gui;

import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class FiguraGuiEntityRenderer extends GuiEntityRenderer {

    public FiguraGuiEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    protected String getTextureLabel() {
        return "figura-entity";
    }
}
