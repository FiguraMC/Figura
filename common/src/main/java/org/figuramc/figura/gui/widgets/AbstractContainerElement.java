package org.figuramc.figura.gui.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainerElement extends AbstractContainerEventHandler implements FiguraTickable, FiguraWidget, NarratableEntry {

    public static final Component HOVERED_ARROW = Component.literal("•");

    protected final List<GuiEventListener> children = new ArrayList<>();

    private int x, y;
    private int width, height;
    private boolean hovered = false;
    private boolean visible = true;

    public AbstractContainerElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void tick() {
        for (GuiEventListener listener : this.children) {
            if (listener instanceof FiguraTickable tickable)
                tickable.tick();
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        for (GuiEventListener listener : this.children) {
            if (listener instanceof Renderable widget)
                widget.render(gui, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GuiEventListener widget = null;

        // update children focused
        for (GuiEventListener children : List.copyOf(this.children())) {
            boolean clicked = children.mouseClicked(mouseX, mouseY, button);
            children.setFocused(clicked);
            if (clicked) widget = children;
        }

        // set this focused
        if (getFocused() != widget)
            setFocused(widget);

        if (widget != null) {
            if (button == 0) this.setDragging(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return UIHelper.isMouseOver(getX(), getY(), getWidth(), getHeight(), mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // yeet mouse 0 and isDragging check
        return this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // better check for mouse released when outside node's boundaries
        boolean bool = this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);

        // remove focused when clicking
        if (bool) setFocused(null);

        this.setDragging(false);
        return bool;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double d) {
        // fix scrolling targeting only one child
        boolean ret = false;
        for (GuiEventListener child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY))
                ret = ret || child.mouseScrolled(mouseX, mouseY, amount, d);
        }
        return ret;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;

        for (GuiEventListener listener : this.children) {
            if (listener instanceof FiguraWidget drawable)
                drawable.setVisible(visible);
            else if (listener instanceof AbstractWidget widget)
                widget.visible = visible;
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) setFocused(null);
        super.setFocused(focused);
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }
}
