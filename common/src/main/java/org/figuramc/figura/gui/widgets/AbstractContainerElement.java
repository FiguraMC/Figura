package org.figuramc.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.figuramc.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainerElement extends AbstractContainerEventHandler implements FiguraTickable, FiguraWidget {

    public static final Component HOVERED_ARROW = new TextComponent("•");

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
            if (listener instanceof FiguraTickable) {
                FiguraTickable tickable = (FiguraTickable) listener;
                tickable.tick();
            }
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        for (GuiEventListener listener : this.children) {
            if (listener instanceof Widget) {
                Widget widget = (Widget) listener;
                widget.render(stack, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //fix mojang focusing for text fields
        for (GuiEventListener listener : this.children) {
            if (listener instanceof TextField) {
                TextField field = (TextField) listener;
                field.getField().setFocus(field.isEnabled() && field.isMouseOver(mouseX, mouseY));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        return this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // fix scrolling targeting only one child
        boolean ret = false;
        for (GuiEventListener child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY))
                ret = ret || child.mouseScrolled(mouseX, mouseY, amount);
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
            if (listener instanceof FiguraWidget) {
                FiguraWidget drawable = (FiguraWidget) listener;
                drawable.setVisible(visible);
            } else if (listener instanceof AbstractWidget) {
                AbstractWidget widget = (AbstractWidget) listener;
                widget.visible = visible;
            }
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
}