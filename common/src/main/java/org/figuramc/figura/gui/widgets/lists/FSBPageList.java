package org.figuramc.figura.gui.widgets.lists;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.figuramc.figura.gui.widgets.FiguraWidget;
import org.figuramc.figura.gui.widgets.fsb_pages.PageButton;
import org.figuramc.figura.utils.ColorUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FSBPageList extends AbstractList {

    private final List<FiguraWidget> content;
    int currentPageButton = 0;

    /**
     * stream of {@link #content} elements that extend {@link GuiEventListener}.
     */
    private Stream<GuiEventListener> contentWithListeners() {
        return content.stream().filter(it -> it instanceof GuiEventListener).map(it -> (GuiEventListener) it);
    }

    public FSBPageList(int x, int y, int width, int height, List<FiguraWidget> content) {
        super(x, y, width, height);
        relayout();
        this.content = content;
        this.children.addAll(contentWithListeners().toList());
        updateScissors(1, 1, -1, -2);
    }

    public void updateContent(Consumer<List<FiguraWidget>> updater) {
        this.children.removeAll(contentWithListeners().toList());
        updater.accept(content);
        this.children.addAll(contentWithListeners().toList());
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
