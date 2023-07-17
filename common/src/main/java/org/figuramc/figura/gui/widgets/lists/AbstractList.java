package org.figuramc.figura.gui.widgets.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.figuramc.figura.gui.widgets.AbstractContainerElement;
import org.figuramc.figura.gui.widgets.ScrollBarWidget;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.Collections;
import java.util.List;

public abstract class AbstractList extends AbstractContainerElement {

    protected final ScrollBarWidget scrollBar;

    public int scissorsX, scissorsY;
    public int scissorsWidth, scissorsHeight;

    public AbstractList(int x, int y, int width, int height) {
        super(x, y, width, height);

        updateScissors(1, 1, -2, -2);

        children.add(scrollBar = new ScrollBarWidget(x + width - 14, y + 4, 10, height - 8, 0d));
        scrollBar.setVisible(false);
    }

    public void updateScissors(int xOffset, int yOffset, int endXOffset, int endYOffset) {
        this.scissorsX = xOffset;
        this.scissorsY = yOffset;
        this.scissorsWidth = endXOffset;
        this.scissorsHeight = endYOffset;
    }

    public boolean isInsideScissors(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(getX() + scissorsX, getY() + scissorsY, getWidth() + scissorsWidth, getHeight() + scissorsHeight, mouseX, mouseY);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        for (GuiEventListener child : children) {
            if (child instanceof Widget widget && !contents().contains(child))
                widget.render(stack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return scrollBar.mouseScrolled(mouseX, mouseY, amount) || super.mouseScrolled(mouseX, mouseY, amount);
    }

    public List<? extends GuiEventListener> contents() {
        return Collections.emptyList();
    }
}