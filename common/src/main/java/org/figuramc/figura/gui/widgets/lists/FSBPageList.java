package org.figuramc.figura.gui.widgets.lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.gui.screens.FSBScreen;
import org.figuramc.figura.gui.widgets.fsb_pages.PageButton;
import org.figuramc.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class FSBPageList extends AbstractList {

    private final List<PageButton> buttons = new ArrayList<>();
    int currentPageButton = 0;

    private final FSBScreen parent;

    public FSBPageList(int x, int y, int width, int height, FSBScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
        relayout();
        for (int i = 0; i < 30; i++) {
            PageButton btn = PageButton.of(
                    Component.literal("hi!"), null, q -> {
                    }, 0xffb080ff
            );
            btn.setHeight(16);
            buttons.add(btn);
        }
        updateScissors(1, 1, -1, -2);
    }

    @Override
    public void relayout() {
        // We want the scrollbar on the left, actually.
        scrollBar.setBox(getX() + 2, getY() + 2, 10, getHeight() - 4);
        scrollBar.setVisible(true);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), width = getWidth(), height = getHeight();

//        enableScissors(gui);
        int totalHeight = 2;

        int i = 0;
        for (PageButton button : buttons) {
            int buttonHeight = button.getHeight();
            boolean active = i == currentPageButton;
            int selectedOffset = active ? 0 : 4;
            int widthOffset = active ? 0 : 1;
            button.isActivePage = active;
            button.setBox(
                    x + 18 + selectedOffset,
                    y + totalHeight,
                    width - 18 - selectedOffset - widthOffset,
                    buttonHeight
            );
            button.render(gui, mouseX, mouseY, delta);
            totalHeight += buttonHeight;
            totalHeight += 1;
            i++;
        }

        gui.fill(x + width - 1, y, x + width, y + height, 0xFF404040);

//        gui.disableScissor();

        super.render(gui, mouseX, mouseY, delta);
    }
}
