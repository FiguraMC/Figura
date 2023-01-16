package org.moon.figura.gui.widgets;

import net.minecraft.client.gui.components.Renderable;

public interface FiguraWidget extends Renderable {
    boolean isVisible();
    void setVisible(boolean visible);
}
