package org.figuramc.figura.gui.widgets.fsb_pages;

import net.minecraft.client.gui.GuiGraphics;
import org.figuramc.figura.gui.screens.FSBScreen;
import org.figuramc.figura.utils.ui.UIHelper;

public class DebugFSBPage extends AbstractFSBPage {
    @Override
    public boolean isMaximized() {
        return false;
    }

    public DebugFSBPage(int x, int y, int width, int height, FSBScreen parent) {
        super(x, y, width, height);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        gui.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x20b080ff);
//        UIHelper.blitSliced(gui, getX(), getY(), getWidth(), getHeight(), UIHelper.OUTLINE_FILL);
    }
}
