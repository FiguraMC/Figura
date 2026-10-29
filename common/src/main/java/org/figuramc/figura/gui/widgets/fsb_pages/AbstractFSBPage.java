package org.figuramc.figura.gui.widgets.fsb_pages;

import org.figuramc.figura.gui.widgets.AbstractContainerElement;

public abstract class AbstractFSBPage extends AbstractContainerElement {
    public abstract boolean isMaximized();

    protected boolean isActuallyMaximized;

    public void isActuallyMaximized(boolean value) {
        isActuallyMaximized = value;
    }

    public AbstractFSBPage(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}
