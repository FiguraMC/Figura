package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.components.Renderable;

public interface FiguraWidget extends Renderable {
    boolean isVisible();
    void setVisible(boolean visible);
    int getX();
    void setX(int x);
    int getY();
    void setY(int y);
    int getWidth();
    void setWidth(int width);
    int getHeight();
    void setHeight(int height);
}
