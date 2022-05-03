package org.moon.figura.gui.widgets;

import net.minecraft.client.gui.components.Widget;

public interface FiguraWidget extends Widget {
    boolean isVisible();
    void setVisible(boolean visible);
}
