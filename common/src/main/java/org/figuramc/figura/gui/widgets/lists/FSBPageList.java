package org.figuramc.figura.gui.widgets.lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.gui.screens.FSBScreen;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.SpacerWidget;
import org.figuramc.figura.gui.widgets.fsb_pages.PageButton;
import org.figuramc.figura.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class FSBPageList extends AbstractList {

    private final List<FiguraWidget> content = new ArrayList<>();
    int currentPageButton = 0;

    private final FSBScreen parent;

    private void page(Component label, int color) {
        PageButton btn = PageButton.of(
                label, null, q -> {}, color
        );
        btn.setHeight(16);
        content.add(btn);
    }

    public FSBPageList(int x, int y, int width, int height, FSBScreen parent) {
        super(x, y, width, height);
        this.parent = parent;
        relayout();
        page(Component.literal("Connections"), 235);
        page(Component.literal("Client Options"), 235);
        content.add(SpacerWidget.of(0, 8));
        page(Component.literal("FSB for LAN Servers"), 160);
        page(Component.literal("Configuration"), 160);
        page(Component.literal("Players"), 160);
        page(Component.literal("Content"), 160);
        content.add(SpacerWidget.of(0, 8));
        page(Component.literal("server name"), 55);
        page(Component.literal("Configuration"), 55);
        page(Component.literal("Players"), 55);
        page(Component.literal("Content"), 55);
        content.add(SpacerWidget.of(0, 8));
        page(Component.literal("Done"), 0);
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
        PageButton current = null;
        for (FiguraWidget widget : content) {
            int widgetH = widget.getHeight();
            if (widget instanceof PageButton btn) {
                boolean active = i == currentPageButton;
                if (active) current = btn;
                int selectedOffset = active ? 0 : 4;
                int widthOffset = active ? 0 : 1;
                btn.isCurrentPage = active;
                btn.setBox(
                        x + 18 + selectedOffset,
                        y + totalHeight,
                        width - 18 - selectedOffset - widthOffset,
                        widgetH
                );
                btn.render(gui, mouseX, mouseY, delta);
                i++;
            }
            totalHeight += widgetH;
            totalHeight += 1;
        }

        int stripeColor = current != null ? ColorUtils.rgbToInt(current.getSelectedColor()) | 0xFF000000 : 0xFF404040;
        gui.fill(x + width - 1, y, x + width, y + height, stripeColor);

//        gui.disableScissor();

        super.render(gui, mouseX, mouseY, delta);
    }
}
