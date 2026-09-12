package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class SpacerWidget implements FiguraWidget {
    private int x;
    private int y;
    private int w;
    private int h;

    public SpacerWidget(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public static SpacerWidget of(int w, int h) {
        return new SpacerWidget(120, 30, w, h);
    }

    private boolean visibility = false;

    /**
     * This really means nothing on a widget that renders nothing.
     */
    @Override
    public boolean isVisible() {
        return visibility;
    }

    @Override
    public void setVisible(boolean visible) {
        visibility = visible;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public void setWidth(int width) {
        this.w = width;
    }

    @Override
    public int getHeight() {
        return h;
    }

    @Override
    public void setHeight(int height) {
        this.h = height;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Nothing
    }
}
