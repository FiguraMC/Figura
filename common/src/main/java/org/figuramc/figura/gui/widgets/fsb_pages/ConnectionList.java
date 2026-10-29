package org.figuramc.figura.gui.widgets.fsb_pages;

import net.minecraft.client.gui.GuiGraphics;
import org.figuramc.figura.gui.screens.FSBScreen;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.utils.ColorUtils;

public class ConnectionList extends AbstractFSBPage {
    private static final FiguraVec3 BG_TINT_V = ColorUtils.ColorTheme.of(FSBScreen.SECTION_CLIENT).stop(5);
    private static final int BG_TINT = ColorUtils.rgbToInt(BG_TINT_V) | 0x20000000;

    @Override
    public boolean isMaximized() {
        return false;
    }

    public ConnectionList(int x, int y, int width, int height, FSBScreen parent) {
        super(x, y, width, height);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        gui.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), BG_TINT);
//        UIHelper.blitSliced(gui, getX(), getY(), getWidth(), getHeight(), UIHelper.OUTLINE_FILL);
    }
}
