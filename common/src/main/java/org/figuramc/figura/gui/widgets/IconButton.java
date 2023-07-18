package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.ui.UIHelper;

public class IconButton extends Button {

    public IconButton(int x, int y, int width, int height, int u, int v, int regionSize, ResourceLocation texture, int textureWidth, int textureHeight, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, text, tooltip, pressAction);
    }

    @Override
    protected void renderTexture(PoseStack stack, float delta) {
        this.renderDefaultTexture(stack, delta);

        UIHelper.setupTexture(texture);
        int size = getTextureSize();
        blit(stack, getX() + 2, getY() + (getHeight() - size) / 2, size, size, u, v, regionSize, regionSize, textureWidth, textureHeight);
    }

    @Override
    protected void renderText(PoseStack stack, float delta) {
        int size = getTextureSize();
        UIHelper.renderCenteredScrollingText(stack, getMessage(), getX() + 4 + size, getY(), getWidth() - 6 - size, getHeight(), getTextColor());
    }

    protected int getTextureSize() {
        return Math.min(getWidth(), getHeight()) - 4;
    }
}
