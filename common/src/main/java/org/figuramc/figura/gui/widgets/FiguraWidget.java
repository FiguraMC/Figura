package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.components.Widget;

public interface FiguraWidget extends Widget {
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
