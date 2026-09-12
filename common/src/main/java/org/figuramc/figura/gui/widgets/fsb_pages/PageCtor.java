package org.figuramc.figura.gui.widgets.fsb_pages;

import org.figuramc.figura.gui.screens.FSBScreen;

@FunctionalInterface
public interface PageCtor<T> {
    T ctor(int x, int y, int width, int height, FSBScreen parent);

    default T auto(FSBScreen parent) {
        return ctor(16, 16, 256, 256, parent);
    }
}
