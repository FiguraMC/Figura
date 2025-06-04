package org.figuramc.figura.gui;

import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class FiguraGuiEntityRenderer extends GuiEntityRenderer {

    public FiguraGuiEntityRenderer(MultiBufferSource.BufferSource bufferSource, EntityRenderDispatcher entityRenderDispatcher) {
        super(bufferSource, entityRenderDispatcher);
    }

    @Override
    protected String getTextureLabel() {
        return "figura-entity";
    }
}
